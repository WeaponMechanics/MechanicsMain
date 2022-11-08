package me.deecaad.weaponmechanics.mechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.utils.LogLevel;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.*;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;
import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class Mechanics implements Serializer<Mechanics> {

    private static final Set<String> registeredMechanics = new HashSet<>();
    private static final Map<String, IMechanic<?>> mechanicSerializers = new HashMap<>();
    private List<IMechanic<?>> mechanicList;

    /**
     * Default constructor for serializer
     */
    public Mechanics() {
    }

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

    /**
     * Use all registered mechanics under specific path with given cast data.
     *
     * @param path the path to mechanics (e.g. WeaponName.Damage)
     * @param castData the cast data for this use of mechanics
     */
    public static void use(String path, CastData castData) {
        for (String keyword : registeredMechanics) {

            IMechanic<?> mechanic;
            try {
                mechanic = getConfigurations().getObject(path + "." + keyword, IMechanic.class);
            } catch (ClassCastException exc) {
                debug.log(LogLevel.ERROR,
                        "Tried to get mechanic using keyword " + keyword + " at path " + path + ", but"
                                + " couldn't cast it to IMechanic class?",
                                "This might be plugin sided issue or this keyword was used somewhere it wasn't supposed to.",
                                "Or serializer at this path didn't work properly because of misconfiguration.");
                continue;
            }

            if (mechanic == null) continue;
            if (mechanic.requireEntity() && castData.getCaster() == null) continue;
            if (mechanic.requirePlayer() && (castData.getCaster() == null || castData.getCaster().getType() != EntityType.PLAYER)) continue;

            mechanic.use(castData);
        }
    }

    public void use(CastData castData) {
        for (IMechanic<?> mechanic : mechanicList) {
            if (mechanic.requireEntity() && castData.getCaster() == null) continue;
            if (mechanic.requirePlayer() && (castData.getCaster() == null || castData.getCaster().getType() != EntityType.PLAYER)) continue;

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

            Object mechanic = data.of(keyword).serialize(mechanicSerializer);
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