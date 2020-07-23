package me.deecaad.core.mechanics.serialization;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.JarSearcher;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.mechanics.Mechanic;
import me.deecaad.core.mechanics.serialization.datatypes.DataType;
import me.deecaad.core.mechanics.targeters.SelfTargeter;
import me.deecaad.core.mechanics.targeters.Targeter;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static me.deecaad.core.MechanicsCore.debug;

public class MechanicListSerializer implements Serializer<MechanicListSerializer.MechanicList> {

    private static final Targeter<?> DEFAULT = new SelfTargeter();

    private Map<String, Class<Mechanic>> mechanics;
    private Map<String, Class<Targeter>> targeters;

    public MechanicListSerializer() {
        mechanics = new HashMap<>();
        targeters = new HashMap<>();

        JarSearcher searcher = new JarSearcher(MechanicsCore.getPlugin().getJarFile());

        for (Class<Mechanic> clazz : searcher.findAllSubclasses(Mechanic.class, true)) {
            Mechanic mechanic = ReflectionUtil.newInstance(clazz);
            if (mechanic == null) {
                debug.error(clazz + " didn't have an empty constructor!");
                continue;
            }

            String name = mechanic.getName();
            Class<Mechanic> replaced = mechanics.put(name, clazz);

            if (replaced != null) {
                debug.debug("Overridden Mechanic: " + clazz);
            }
        }

        for (Class<Targeter> clazz : searcher.findAllSubclasses(Targeter.class, true)) {
            Targeter<?> targeter = ReflectionUtil.newInstance(clazz);
            if (targeter == null) {
                debug.error(clazz + " didn't have an empty constructor!");
                continue;
            }

            String name = targeter.getName();
            Class<Targeter> replaced = targeters.put(name, clazz);

            if (replaced != null) {
                debug.debug("Overridden Mechanic: " + clazz);
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

                Mechanic mechanic = ReflectionUtil.newInstance(mechanics.get(mechanicTitle));
                Targeter<?> targeter = targeters.containsKey(targeterTitle) ? ReflectionUtil.newInstance(targeters.get(targeterTitle)) : DEFAULT;
                Map<String, Object> mechanicData = getArguments(mechanicTitle, str, mechanic.getArgs());
                Map<String, Object> targeterData = getArguments(targeterTitle, str, mechanic.getArgs());

                // This redundancy is for when the serializer
                // returns a new instance instead of modifying
                // the existing instance
                mechanic = mechanic.serialize(mechanicData);
                targeter = targeter.serialize(targeterData);

                mechanic.setTargeter(targeter);

                temp.add(mechanic);
            }

        } else {

        }
        return new MechanicList(temp);
    }

    public static Map<String, Object> getArguments(String name, String str, Argument[] args) {

        String inParens = StringUtils.match("(?<=" + name + "\\().+(?=\\))", str);
        Map<String, Object> data = new HashMap<>();

        for (String match : inParens.split(" ?[,;] ?")) {
            String[] split = match.split(" ?[:=] ?", 1);

            if (split.length != 2) {
                // Darth Vader Yells: "NOOOOOOOOOOOOOOOOOOOOOOOOO"
            }

            Argument arg = null;
            for (Argument argument : args) {
                if (argument.isArgument(split[0])) {
                    arg = argument;
                    break;
                }
            }

            if (arg == null) {
                // unknown arg
            }

            DataType<?> type = arg.getType();
            if (!type.validate(split[1])) {
                // Invalid type
            }

            data.put(split[0], type.serialize(split[1]));
        }

        return data;
    }
    
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
