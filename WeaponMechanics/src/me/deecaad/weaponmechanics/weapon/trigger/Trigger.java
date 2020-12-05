package me.deecaad.weaponmechanics.weapon.trigger;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlot;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class Trigger implements Serializer<Trigger> {

    private TriggerType mainhand;
    private TriggerType offhand;
    private Set<String> denyWhen;

    /**
     * Empty constructor to be used as serializer
     */
    public Trigger() {
    }

    public Trigger(TriggerType mainhand, TriggerType offhand, Set<String> denyWhen) {
        this.mainhand = mainhand;
        this.offhand = offhand;
        this.denyWhen = denyWhen;
    }

    /**
     * Checks if trigger is valid
     *
     * @param triggerType   the trigger type
     * @param slot          the slot used
     * @param entityWrapper the entity's wrapper from whom to check
     * @return true if trigger is valid
     */
    public boolean check(TriggerType triggerType, EquipmentSlot slot, IEntityWrapper entityWrapper) {
        if (slot == EquipmentSlot.HAND
                // Main and off hand are both optional, but only either one is necessary
                // Thats why this has null checks also
                // Null should mean that check is NOT valid
                ? (this.mainhand == null || this.mainhand != triggerType)
                : (this.offhand == null || this.offhand != triggerType)) {
            // Not the same trigger type
            return false;
        }
        if (denyWhenReloading() && (entityWrapper.getMainHandData().isReloading() || entityWrapper.getOffHandData().isReloading()))
            return false;
        if (denyWhenZooming() && (entityWrapper.getMainHandData().getZoomData().isZooming() || entityWrapper.getOffHandData().getZoomData().isZooming()))
            return false;
        if (denyWhenSneaking() && entityWrapper.isSneaking()) return false;
        if (denyWhenStanding() && entityWrapper.isStanding()) return false;
        if (denyWhenWalking() && entityWrapper.isWalking()) return false;
        if (denyWhenSwimming() && entityWrapper.isSwimming()) return false;
        if (denyWhenInMidair() && entityWrapper.isInMidair()) return false;
        return !denyWhenGliding() || !entityWrapper.isGliding();
    }

    /**
     * @return the trigger for main hand
     */
    public TriggerType getMainhand() {
        return this.mainhand;
    }

    /**
     * @return the trigger for off hand
     */
    public TriggerType getOffhand() {
        return this.offhand;
    }

    /**
     * @return true if trigger should be cancelled while reloading
     */
    public boolean denyWhenReloading() {
        return denyWhen.contains("Reloading");
    }

    /**
     * @return true if trigger should be cancelled while zooming
     */
    public boolean denyWhenZooming() {
        return denyWhen.contains("Zooming");
    }

    /**
     * @return true if trigger should be cancelled while sneaking
     */
    public boolean denyWhenSneaking() {
        return denyWhen.contains("Sneaking");
    }

    /**
     * @return true if trigger should be cancelled while standing
     */
    public boolean denyWhenStanding() {
        return denyWhen.contains("Standing");
    }

    /**
     * @return true if trigger should be cancelled while walking
     */
    public boolean denyWhenWalking() {
        return denyWhen.contains("Walking");
    }

    /**
     * @return true if trigger should be cancelled while swimming
     */
    public boolean denyWhenSwimming() {
        return denyWhen.contains("Swimming");
    }

    /**
     * @return true if trigger should be cancelled while in midair
     */
    public boolean denyWhenInMidair() {
        return denyWhen.contains("In_Midair");
    }

    /**
     * @return true if trigger should be cancelled while gliding
     */
    public boolean denyWhenGliding() {
        return denyWhen.contains("Gliding");
    }

    @Override
    public String getKeyword() {
        return "Trigger";
    }

    @Override
    public Trigger serialize(File file, ConfigurationSection configurationSection, String path) {
        String main = configurationSection.getString(path + ".Main_Hand");
        String off = configurationSection.getString(path + ".Off_Hand");
        if (main == null && off == null) {
            return null;
        }
        TriggerType mainTrigger = null;
        if (main != null) {
            main = main.toUpperCase();
            try {
                mainTrigger = TriggerType.valueOf(main);
            } catch (IllegalArgumentException e) {
                debug.log(LogLevel.ERROR,
                        "Found an invalid trigger type in configurations!",
                        "Located at file " + file + " in " + path + ".Main_Hand" + " (" + main + ") in configurations");
                return null;
            }
        }
        TriggerType offTrigger = null;
        if (off != null) {
            off = off.toUpperCase();
            try {
                offTrigger = TriggerType.valueOf(off);
            } catch (IllegalArgumentException e) {
                debug.log(LogLevel.ERROR,
                        "Found an invalid trigger type in configurations!",
                        "Located at file " + file + " in " + path + ".Off_Hand" + " (" + off + ") in configurations");
                return null;
            }
        }
        Set<String> denyWhen = new HashSet<>();
        ConfigurationSection denySection = configurationSection.getConfigurationSection(path + ".Deny_When");
        if (denySection != null) {
            for (String denyName : denySection.getKeys(false)) {

                if (!denyName.equals("Reloading") && !denyName.equals("Zooming") && !denyName.equals("Sneaking") && !denyName.equals("Standing")
                        && !denyName.equals("Walking") && !denyName.equals("Swimming") && !denyName.equals("In_Midair") && !denyName.equals("Gliding")) {
                    debug.log(LogLevel.ERROR,
                            "Found and invalid deny when value in configurations!",
                            "Located at file " + file + " in " + path + ".Deny_When." + denyWhen + " (" + denyWhen + ") in configurations",
                            "Valid ones are: Reloading, Zooming, Sneaking, Standing, Walking, Swimming, In_Midair and Gliding");
                    continue;
                }

                boolean denied = configurationSection.getBoolean(path + ".Deny_When." + denyName, false);
                if (denied) {
                    denyWhen.add(denyName);
                }
            }
        }
        if (mainTrigger == null && offTrigger == null) {
            return null;
        }
        return new Trigger(mainTrigger, offTrigger, denyWhen);
    }
}
