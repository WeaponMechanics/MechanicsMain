package me.deecaad.core.mechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.utils.LogLevel;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.*;

import static me.deecaad.core.MechanicsCore.debug;

public class Mechanics implements Serializer<Mechanics> {

    private static final Set<String> registeredMechanics = new HashSet<>();
    private static final Map<String, IMechanic<?>> mechanicSerializers = new HashMap<>();
    private List<IMechanic<?>> mechanicList;

    /**
     * Default constructor for serializer
     */
    public Mechanics() { }

    public Mechanics(List<IMechanic<?>> mechanicList) {
        this.mechanicList = mechanicList;
    }

    /**
     * @param keyword the keyword to check
     * @return whether mechanic exists with given keyword
     */
    public static boolean hasMechanic(String keyword) {
        return registeredMechanics.contains(keyword);
    }

    /**
     * Register new mechanic keyword
     *
     * @param plugin the registering plugin's instance
     * @param mechanic the mechanic instance
     */
    public static void registerMechanic(Plugin plugin, IMechanic<?> mechanic) {
        if (plugin == null) throw new NullPointerException("Plugin can't be null...");
        if (mechanic == null) throw new NullPointerException("Mechanic can't be null...");
        if (registeredMechanics.contains(mechanic.getKeyword())) {
            debug.log(LogLevel.ERROR,
                    "Plugin " + plugin.getName() + " tried to add duplicate mechanic keyword, skipping... (" + mechanic.getKeyword() + ")");
            return;
        }
        registeredMechanics.add(mechanic.getKeyword());
        mechanicSerializers.put(mechanic.getKeyword(), mechanic);
    }

    public void use(CastData castData) {
        for (IMechanic<?> mechanic : mechanicList) {
            if (mechanic.requireEntity() && castData.getCaster() == null) continue;
            if (mechanic.requirePlayer() && (castData.getCaster() == null
                    || castData.getCaster().getType() != EntityType.PLAYER)) continue;

            mechanic.use(castData);
        }
    }

    @Override
    public String getKeyword() {
        return "Mechanics";
    }

    @Override
    @Nonnull
    public Mechanics serialize(SerializeData data) throws SerializerException {

        List<IMechanic<?>> mechanicsList = new ArrayList<>();

        for (String keyword : registeredMechanics) {

            IMechanic<?> mechanicSerializer = mechanicSerializers.get(keyword);
            if (mechanicSerializer == null)
                continue;

            Object mechanic = data.of(keyword).serializeNonStandardSerializer(mechanicSerializer);
            if (mechanic == null)
                continue;

            mechanicsList.add((IMechanic<?>) mechanic);
        }

        if (mechanicsList.isEmpty()) {
            throw data.exception(null, "Found an empty Mechanics list. You should define at least one Mechanic, or remove the list.");
        }

        return new Mechanics(mechanicsList);
    }
}