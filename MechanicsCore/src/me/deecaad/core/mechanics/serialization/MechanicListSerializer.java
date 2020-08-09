package me.deecaad.core.mechanics.serialization;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.mechanics.Mechanic;
import me.deecaad.core.mechanics.targeters.SelfTargeter;
import me.deecaad.core.mechanics.targeters.Targeter;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static me.deecaad.core.MechanicsCore.debug;

@SuppressWarnings("unchecked")
public class MechanicListSerializer implements Serializer<MechanicListSerializer.MechanicList> {

    // Default serializer, can be changed via reflection if need be
    private static final Targeter<?> DEFAULT = new SelfTargeter();

    private Map<String, Class<Mechanic>> mechanics;
    private Map<String, Class<Targeter>> targeters;

    public MechanicListSerializer() {
        mechanics = new HashMap<>();
        targeters = new HashMap<>();

        Map<String, Class<StringSerializable>> serializers = MechanicsCore.getPlugin().getStringSerializers();
        for (Map.Entry<String, Class<StringSerializable>> entry : serializers.entrySet()) {

            String name = entry.getKey();
            Class<StringSerializable> clazz = entry.getValue();

            if (Mechanic.class.isAssignableFrom(clazz)) {
                Class<Mechanic> replaced = mechanics.put(name, (Class<Mechanic>)((Class<?>)clazz));
                if (replaced != null) debug.debug("Overridden mechanic: " + replaced);

            } else if (Targeter.class.isAssignableFrom(clazz)) {
                Class<Targeter> replaced = targeters.put(name, (Class<Targeter>)((Class<?>)clazz));
                if (replaced != null) debug.debug("Overridden targeter: " + replaced);
            }
        }
    }
    
    @Override
    public String getKeyword() {
        return "Mechanics";
    }

    @Override
    public MechanicList serialize(File file, ConfigurationSection configurationSection, String path) {

        List<String> strings = configurationSection.getList(path, Collections.EMPTY_LIST).stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        List<Mechanic> temp = new ArrayList<>();

        if (!strings.isEmpty()) {

            for (String str : strings) {

                // Determine which mechanic/targeter is used
                String mechanicTitle = StringUtils.match("[^( ]+", str);
                String targeterTitle = StringUtils.match("@[^( ]+", str);

                if (!mechanics.containsKey(mechanicTitle)) {
                    debug.error("Unknown mechanic: " + mechanicTitle, "Found at: " + str);
                    continue;
                } else if (!targeters.containsKey(targeterTitle)) {
                    debug.error("Unknown targeter: " + targeterTitle, "Found at: " + str);
                    continue;
                }

                Mechanic mechanic = ReflectionUtil.newInstance(mechanics.get(mechanicTitle));
                Targeter<?> targeter = targeters.containsKey(targeterTitle) ? ReflectionUtil.newInstance(targeters.get(targeterTitle)) : DEFAULT;
                Argument[] mechanicArgs = StringSerializable.parseArgs(mechanics.get(mechanicTitle));
                Argument[] targeterArgs = StringSerializable.parseArgs(targeters.get(targeterTitle));
                Map<String, Object> mechanicData = getArguments(mechanicTitle, str, mechanicArgs);
                Map<String, Object> targeterData = getArguments(targeterTitle, str, targeterArgs);
                
                Mechanic tempMechanic = mechanic.serialize(mechanicData);
                Targeter tempTargeter = targeter.serialize(targeterData);

                if (tempMechanic != mechanic || tempTargeter != targeter) {
                    debug.warn("DEVELOPERS: You should not return a new instance of the StringSerializable...",
                            "...during data serialization. This is not an error, but it is violation of contract");
                }
                
                mechanic.setTargeter(tempTargeter);

                temp.add(tempMechanic);
            }

        } else {
            ConfigurationSection config = configurationSection.getConfigurationSection(path);
            for (String key: config.getKeys(false)) {
                Class<Mechanic> clazz = mechanics.get(key.substring(0, key.length() - 2));
                
                if (clazz == null) {
                    debug.error("Unknown mechanic key: " + key);
                    debug.error("");
                }
            }
        }
        return new MechanicList(temp);
    }

    public static Map<String, Object> getArguments(String name, String str, Argument[] args) {

        String inParens = StringUtils.match("(?<=" + name + "\\().*?(?=\\))", str);
        Map<String, Object> data = new HashMap<>();

        // Checking to see if the user gave arguments.
        // If not, this may be an error, so alert the user
        if (inParens == null) {
            if (args.length != 0) {
                debug.warn("Failed to specify arguments for: " + name,
                        "THIS MAY NOT BE AN ERROR! Possible arguments: " + Arrays.toString(args));
            }
            return data;
        }

        // Splitting up all the arguments. Example:
        // "arg1=0; arg2=test; arg3=true"
        for (String match : inParens.split(" ?[,;] ?")) {
            
            // Splitting the variables name (split[0]) and value (split[1])
            String[] split = match.split(" ?[:=] ?", 2);

            // Determine which argument is being used
            Argument arg = null;
            for (Argument argument : args) {
                if (argument.isArgument(split[0])) {
                    arg = argument;
                    break;
                }
            }

            // Inform the user of unknown arguments, incorrect
            // variable declarations and invalid types
            if (arg == null) {
                debug.error("Unknown argument: " + split[0],
                        "Valid arguments: " + Arrays.toString(args),
                        "Found at: " + str);
                continue;
            } else if (split.length != 2) {
                debug.error("Failed to specify value for argument: " + split[0],
                        "Valid arguments: " + Arrays.toString(args),
                        "Found at: " + str);
                continue;
            } else if (!arg.getType().validate(split[1])) {
                debug.error("Invalid data type for arg: " + arg + "!", "Expected: " + arg.getType(), "Got: " + split[1], "Found at: " + str);
                continue;
            }

            data.put(split[0], arg.getType().serialize(split[1]));
        }

        return data;
    }
    
    /**
     * Since <code>Configuration</code> classes check for lists
     * and converts them into string lists, we have to use this
     * wrapper class
     *
     * In the future, it would be nice to implement our own parser
     * and implement serializers during parsing
     */
    public static final class MechanicList {
        
        private final List<Mechanic> mechanics;
        
        public MechanicList(List<Mechanic> mechanics) {
            this.mechanics = mechanics;
        }
        
        public List<Mechanic> getMechanics() {
            return mechanics;
        }
    }
}
