package me.deecaad.weaponmechanics.weapon.trigger;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.SerializerOptionsException;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlot;

import javax.annotation.Nonnull;
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
    public Trigger() {}

    public Trigger(TriggerType mainhand, TriggerType offhand, Set<String> denyWhen) {
        this.mainhand = mainhand;
        this.offhand = offhand;
        this.denyWhen = denyWhen;
    }

    /**
     * Checks if trigger is valid
     *
     * @param triggerType the trigger type
     * @param slot the slot used
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
        if (denyWhenReloading() && (entityWrapper.getMainHandData().isReloading() || entityWrapper.getOffHandData().isReloading())) return false;
        if (denyWhenZooming() && (entityWrapper.getMainHandData().getZoomData().isZooming() || entityWrapper.getOffHandData().getZoomData().isZooming())) return false;
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
    @Nonnull
    public Trigger serialize(SerializeData data) throws SerializerException {
        TriggerType main = data.of("Main_Hand").getEnum(TriggerType.class, null);
        TriggerType off = data.of("Off_Hand").getEnum(TriggerType.class, null);

        if (main == null && off == null) {
            data.throwException(null, "At least one of Main_Hand or Off_Hand should be used");
        }

        Set<String> denyWhen = new HashSet<>();
        ConfigurationSection denySection = data.config.getConfigurationSection(data.key + ".Deny_When");
        if (denySection != null) {
            for (String denyName : denySection.getKeys(false)) {

                if (!denyName.equals("Reloading") && !denyName.equals("Zooming") && !denyName.equals("Sneaking") && !denyName.equals("Standing")
                        && !denyName.equals("Walking") && !denyName.equals("Swimming") && !denyName.equals("In_Midair") && !denyName.equals("Gliding")) {

                    data.throwException("Deny_When", "Unknown key: " + denyName);
                }

                boolean denied = data.of("Deny_When." + denyName).assertType(Boolean.class).get(false);
                if (denied)
                    denyWhen.add(denyName);
            }
        }

        return new Trigger(main, off, denyWhen);
    }
}
