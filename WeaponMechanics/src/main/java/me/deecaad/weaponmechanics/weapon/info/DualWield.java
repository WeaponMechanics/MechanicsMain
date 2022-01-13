package me.deecaad.weaponmechanics.weapon.info;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.weapon.trigger.Trigger;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class DualWield implements Serializer<DualWield> {

    private boolean whitelist;
    private Set<String> weapons;
    private Mechanics mechanics;

    /**
     * Empty constructor to be used as serializer
     */
    public DualWield() { }

    public DualWield(boolean whitelist, Set<String> weapons, Mechanics mechanics) {
        this.whitelist = whitelist;
        this.weapons = weapons;
        this.mechanics = mechanics;
    }

    /**
     * Checks whether or not this can be used with other weapon title
     *
     * @param weaponTitle the other weapon title
     * @return true only if dual wielding is allowed
     */
    public boolean denyDualWieldingWith(String weaponTitle) {
        if (!whitelist) {
            // If blacklist and list contains weapon title
            // -> dual wield is not allowed
            return weapons.contains(weaponTitle.toLowerCase());
        }
        // If whitelist and list DOES not contains weapon title
        // -> dual wield is not allowed
        return !weapons.contains(weaponTitle.toLowerCase());
    }

    /**
     * Simply sends dual wield denied message for player.
     * Message is only sent if dual wield check cause is same as
     * weapon's shoot, scope or reload trigger type.
     *
     * @param checkCause the cause of this dual wield check
     * @param player the player for who to send
     * @param weaponTitle the weapon used
     */
    public void sendDeniedMessage(TriggerType checkCause, @Nullable Player player, String weaponTitle) {
        if (player != null) {
            Configuration config = WeaponMechanics.getConfigurations();
            for (String type : new String[]{ ".Shoot", ".Reload", ".Scope" }) {
                Trigger trigger = config.getObject(weaponTitle + type + ".Trigger", Trigger.class);
                if (trigger != null && (trigger.getMainhand() == checkCause || trigger.getOffhand() == checkCause)) {

                    if (mechanics != null) mechanics.use(new CastData(WeaponMechanics.getEntityWrapper(player), weaponTitle, null));

                    break;
                }
            }
        }
    }

    @Override
    public String getKeyword() {
        return "Dual_Wield";
    }

    @Override
    @Nonnull
    public DualWield serialize(SerializeData data) throws SerializerException {
        List<?> weaponsList = configurationSection.getList(path + ".Weapons");
        Set<String> weapons = new HashSet<>();

        try {
            // Saves weapons in lower case
            weaponsList.forEach(weaponTitle -> weapons.add(weaponTitle.toString().toLowerCase()));
        } catch (ClassCastException e) {
            debug.log(LogLevel.ERROR,
                    "Found an invalid value in configurations!",
                    "Located at file " + file + " in " + path + ".Weapons (" + weaponsList + ") in configurations",
                    "Tried to get get weapon title from " + weaponsList + ", but some of its values wasn't string?");
            return null;
        }
        boolean whitelist = configurationSection.getBoolean(path + ".Whitelist", false);
        Mechanics mechanics = new Mechanics().serialize(file, configurationSection, path + ".Mechanics_On_Deny");
        return new DualWield(whitelist, weapons, mechanics);
    }
}