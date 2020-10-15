package me.deecaad.weaponmechanics.mechanics;

import me.deecaad.core.utils.LogLevel;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;
import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class Mechanics {

    private static final Set<String> registeredMechanics = new HashSet<>();

    public static void registerMechanic(Plugin plugin, String keyword) {
        if (plugin == null) throw new NullPointerException("Plugin can't be null...");
        if (keyword == null) throw new NullPointerException("Keyword can't be null...");
        if (registeredMechanics.contains(keyword)) {
            debug.log(LogLevel.ERROR,
                    "Plugin " + plugin.getName() + " tried to add duplicate mechanic keyword, skipping... (" + keyword + ")");
            return;
        }
        registeredMechanics.add(keyword);
    }

    /**
     * Use all registered mechanics under specific path with given cast data.
     *
     * @param path the path to mechanics (e.g. WeaponName.Damage)
     * @param castData the cast data for this use of mechanics
     */
    public static void use(String path, CastData castData) {
        for (String keyword : registeredMechanics) {

            IMechanic mechanic = getConfigurations().getObject(path + "." + keyword, IMechanic.class);
            if (mechanic == null) continue;
            if (mechanic.requireEntity() && castData.getCaster() == null) continue;
            if (mechanic.requirePlayer() && (castData.getCaster() == null || castData.getCaster().getType() != EntityType.PLAYER)) continue;

            mechanic.use(castData);
        }
    }
}