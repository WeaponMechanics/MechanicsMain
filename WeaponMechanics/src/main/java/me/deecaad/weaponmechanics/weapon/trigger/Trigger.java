package me.deecaad.weaponmechanics.weapon.trigger;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.MainHand;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

import static me.deecaad.weaponmechanics.WeaponMechanics.getBasicConfigurations;

public class Trigger implements Serializer<Trigger> {

    private TriggerType mainhand;
    private TriggerType offhand;
    private Set<String> denyWhen;
    private TriggerType dualWieldMainHand;
    private TriggerType dualWieldOffHand;

    /**
     * Empty constructor to be used as serializer
     */
    public Trigger() {}

    public Trigger(TriggerType mainhand, TriggerType offhand, Set<String> denyWhen, TriggerType dualWieldMainHand, TriggerType dualWieldOffHand) {
        this.mainhand = mainhand;
        this.offhand = offhand;
        this.denyWhen = denyWhen;
        this.dualWieldMainHand = dualWieldMainHand;
        this.dualWieldOffHand = dualWieldOffHand;
    }

    /**
     * Checks if trigger is valid
     *
     * @param triggerType the trigger type
     * @param slot the slot used
     * @param entityWrapper the entity's wrapper from whom to check
     * @return true if trigger is valid
     */
    public boolean check(TriggerType triggerType, EquipmentSlot slot, EntityWrapper entityWrapper) {
        TriggerType typeCheck;
        boolean isDual = entityWrapper.isDualWielding();
        LivingEntity livingEntity = entityWrapper.getEntity();

        if (slot == EquipmentSlot.HAND) {
            if (isDual && dualWieldMainHand != null) {
                typeCheck = dualWieldMainHand;
                if (typeCheck.isRightOrLeft() && livingEntity.getType() == EntityType.PLAYER &&((Player) livingEntity).getMainHand() == MainHand.LEFT) {
                    // Invert if player has inverted main hand...
                    typeCheck = typeCheck == TriggerType.RIGHT_CLICK ? TriggerType.LEFT_CLICK : TriggerType.RIGHT_CLICK;
                }
            } else {
                typeCheck = mainhand;
            }
        } else {
            if (isDual && dualWieldOffHand != null) {
                typeCheck = dualWieldOffHand;
                if (typeCheck.isRightOrLeft() && livingEntity.getType() == EntityType.PLAYER &&((Player) livingEntity).getMainHand() == MainHand.LEFT) {
                    // Invert if player has inverted main hand...
                    typeCheck = typeCheck == TriggerType.RIGHT_CLICK ? TriggerType.LEFT_CLICK : TriggerType.RIGHT_CLICK;
                }
            } else {
                typeCheck = offhand;
            }
        }

        if (typeCheck == null || typeCheck != triggerType) return false;

        return checkDeny(entityWrapper);
    }

    /**
     * @return true if valid
     */
    public boolean checkDeny(EntityWrapper entityWrapper) {
        if (denyWhenReloading() && (entityWrapper.getMainHandData().isReloading() || entityWrapper.getOffHandData().isReloading())) return false;
        if (denyWhenZooming() && (entityWrapper.getMainHandData().getZoomData().isZooming() || entityWrapper.getOffHandData().getZoomData().isZooming())) return false;
        if (denyWhenSneaking() && entityWrapper.isSneaking()) return false;
        if (denyWhenStanding() && entityWrapper.isStanding()) return false;
        if (denyWhenWalking() && entityWrapper.isWalking()) return false;
        if (denyWhenSprinting() && entityWrapper.isSprinting()) return false;
        if (denyWhenDualWielding() && entityWrapper.isDualWielding()) return false;
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
     * @return true if trigger should be cancelled when sprinting
     */
    public boolean denyWhenSprinting() {
        return denyWhen.contains("Sprinting");
    }

    /**
     * @return true if trigger should be cancelled when dual wielding
     */
    public boolean denyWhenDualWielding() {
        return denyWhen.contains("Dual_Wielding");
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
            throw data.exception(null, "At least one of Main_Hand or Off_Hand should be used");
        }

        TriggerType dualMain = data.of("Dual_Wield.Main_Hand").getEnum(TriggerType.class, null);
        TriggerType dualOff = data.of("Dual_Wield.Off_Hand").getEnum(TriggerType.class, null);

        if (isDisabled(main)) throw data.exception("Main_Hand", "Tried to use trigger which is disabled in config.yml");
        if (isDisabled(off)) throw data.exception("Off_Hand", "Tried to use trigger which is disabled in config.yml");
        if (isDisabled(dualMain)) throw data.exception("Dual_Wield.Main_Hand", "Tried to use trigger which is disabled in config.yml");
        if (isDisabled(dualOff)) throw data.exception("Dual_Wield.Off_Hand", "Tried to use trigger which is disabled in config.yml");

        Set<String> denyWhen = new HashSet<>();
        ConfigurationSection denySection = data.config.getConfigurationSection(data.key + ".Deny_When");
        if (denySection != null) {
            for (String denyName : denySection.getKeys(false)) {

                if (!denyName.equals("Reloading") && !denyName.equals("Zooming") && !denyName.equals("Sneaking") && !denyName.equals("Standing")
                        && !denyName.equals("Walking") && !denyName.equals("Sprinting") && !denyName.equals("Dual_Wielding")
                        && !denyName.equals("Swimming") && !denyName.equals("In_Midair") && !denyName.equals("Gliding")) {

                    throw data.exception("Deny_When", "Unknown key: " + denyName);
                }

                boolean denied = data.of("Deny_When." + denyName).getBool(false);
                if (denied)
                    denyWhen.add(denyName);
            }
        }

        // Check to make sure the gun denies swapping hands, otherwise this
        // won't work.
        if (dualMain == TriggerType.SWAP_HANDS || dualOff == TriggerType.SWAP_HANDS) {
            String weaponTitle = data.key.split("\\.")[0];

            if (!data.config.getBoolean(weaponTitle + ".Info.Cancel.Swap_Hands", false)) {
                throw data.exception(null, "When using 'SWAP_HANDS', make sure that '" + weaponTitle + ".Info.Cancel.Swap_Hands: true'",
                        SerializerException.forValue(dualMain) + " & " + SerializerException.forValue(dualOff));
            }
        }

        return new Trigger(main, off, denyWhen, dualMain, dualOff);
    }

    private boolean isDisabled(TriggerType trigger) {
        if (trigger == null) return false;
        switch (trigger) {
            case START_SNEAK:
            case END_SNEAK:
            case DOUBLE_SNEAK:
                return getBasicConfigurations().getBool("Disabled_Trigger_Checks.Sneak");
            case START_SPRINT:
            case END_SPRINT:
                return getBasicConfigurations().getBool("Disabled_Trigger_Checks.Sprint");
            case RIGHT_CLICK:
            case LEFT_CLICK:
            case MELEE:
                return getBasicConfigurations().getBool("Disabled_Trigger_Checks.Right_And_Left_Click");
            case DROP_ITEM:
                return getBasicConfigurations().getBool("Disabled_Trigger_Checks.Drop_Item");
            case JUMP:
                return getBasicConfigurations().getBool("Disabled_Trigger_Checks.Jump");
            case DOUBLE_JUMP:
                return getBasicConfigurations().getBool("Disabled_Trigger_Checks.Double_Jump");
            case START_SWIM:
            case END_SWIM:
                return getBasicConfigurations().getBool("Disabled_Trigger_Checks.Swim");
            case START_GLIDE:
            case END_GLIDE:
                return getBasicConfigurations().getBool("Disabled_Trigger_Checks.Glide");
            case SWAP_HANDS:
                return getBasicConfigurations().getBool("Disabled_Trigger_Checks.Swap_Hand_Items");
            case START_WALK:
            case END_WALK:
            case START_STAND:
            case END_STAND:
                return getBasicConfigurations().getBool("Disabled_Trigger_Checks.Standing_And_Walking");
            case START_IN_MIDAIR:
            case END_IN_MIDAIR:
                return getBasicConfigurations().getBool("Disabled_Trigger_Checks.In_Midair");
        }
        return false;
    }
}
