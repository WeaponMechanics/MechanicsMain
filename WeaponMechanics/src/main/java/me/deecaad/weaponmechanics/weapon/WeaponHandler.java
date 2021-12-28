package me.deecaad.weaponmechanics.weapon;

import co.aikar.timings.lib.MCTiming;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.damage.DamageHandler;
import me.deecaad.weaponmechanics.weapon.info.InfoHandler;
import me.deecaad.weaponmechanics.weapon.info.WeaponInfoDisplay;
import me.deecaad.weaponmechanics.weapon.reload.ReloadHandler;
import me.deecaad.weaponmechanics.weapon.reload.ammo.AmmoTypes;
import me.deecaad.weaponmechanics.weapon.scope.ScopeHandler;
import me.deecaad.weaponmechanics.weapon.shoot.ShootHandler;
import me.deecaad.weaponmechanics.weapon.skin.SkinHandler;
import me.deecaad.weaponmechanics.weapon.trigger.Trigger;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;
import static me.deecaad.weaponmechanics.WeaponMechanics.getEntityWrapper;
import static me.deecaad.weaponmechanics.weapon.shoot.SelectiveFireState.*;

/**
 * Class to generally handle weapon functions
 */
public class WeaponHandler {

    private final InfoHandler infoHandler;
    private final ShootHandler shootHandler;
    private final ReloadHandler reloadHandler;
    private final ScopeHandler scopeHandler;
    private final DamageHandler damageHandler;
    private final SkinHandler skinHandler;

    public WeaponHandler() {
        infoHandler = new InfoHandler(this);
        shootHandler = new ShootHandler(this);
        reloadHandler = new ReloadHandler(this);
        scopeHandler = new ScopeHandler(this);
        damageHandler = new DamageHandler(this);
        skinHandler = new SkinHandler(this);
    }

    /**
     * Checks main and off hand items if they're weapons and uses them based on trigger.
     * This is used with the exceptions off PlayerDropItemEvent, PlayerInteractEvent
     * and PlayerSwapHandItemsEvent.
     *
     * @param livingEntity the living entity which caused trigger
     * @param triggerType the trigger type
     * @param autoConvert whether or not the weapon items should be converted
     */
    public void useTrigger(LivingEntity livingEntity, TriggerType triggerType, boolean autoConvert) {

        // This because for all players this should be always nonnull
        // -> No auto add true to deny entity wrappers being added for unnecessary entities (they can also trigger certain
        IEntityWrapper entityWrapper = getEntityWrapper(livingEntity, true);
        if (entityWrapper == null) return;

        if (livingEntity.getType() == EntityType.PLAYER && ((Player) livingEntity).getGameMode() == GameMode.SPECTATOR) return;

        boolean useOffHand = CompatibilityAPI.getVersion() >= 1.09;

        MCTiming timings = WeaponMechanics.timing("Weapon Handlers");
        timings.startTiming();

        // getItemInMainHand didn't exist in 1.8
        ItemStack mainStack = useOffHand ? livingEntity.getEquipment().getItemInMainHand() : livingEntity.getEquipment().getItemInHand();

        String mainWeapon = infoHandler.getWeaponTitle(mainStack, autoConvert);

        // Only get off hand things is server is 1.9 or newer
        ItemStack offStack = null;
        String offWeapon = null;
        if (useOffHand) {
            offStack = livingEntity.getEquipment().getItemInOffHand();
            offWeapon = infoHandler.getWeaponTitle(offStack, autoConvert);
        }

        if (mainWeapon == null && offWeapon == null) {
            timings.stopTiming();
            return;
        }

        // Only do dual wield check if server is 1.9 or newer
        if (useOffHand && infoHandler.denyDualWielding(triggerType, livingEntity.getType() == EntityType.PLAYER ? (Player) livingEntity : null, mainWeapon, offWeapon)) return;

        boolean dualWield = mainWeapon != null && offWeapon != null;

        if (mainWeapon != null) tryUses(entityWrapper, mainWeapon, mainStack, EquipmentSlot.HAND, triggerType, dualWield);

        // Off weapon is automatically null at this point if server is using 1.8
        if (offWeapon != null) tryUses(entityWrapper, offWeapon, offStack, EquipmentSlot.OFF_HAND, triggerType, dualWield);

        timings.stopTiming();
    }

    /**
     * Tries all uses in this exact order
     * <pre>{@code
     * 1) Shoot
     * 2) Reload
     * 3) Scope
     * 4) Selective fire
     * }</pre>
     *
     * @param entityWrapper the entity which caused trigger
     * @param weaponTitle the weapon title involved
     * @param weaponStack the weapon stack involved
     * @param slot the weapon slot used
     * @param triggerType the trigger which caused this
     * @param dualWield whether or not this was dual wield
     */
    public void tryUses(IEntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, TriggerType triggerType, boolean dualWield) {

        // Try shooting
        if (shootHandler.tryUse(entityWrapper, weaponTitle, weaponStack, slot, triggerType, dualWield)) {
            if (triggerType.isSprintType()) getSkinHandler().tryUse(triggerType, entityWrapper, weaponTitle, weaponStack, slot);
            return;
        }

        // Shooting wasn't valid, try reloading
        if (reloadHandler.tryUse(entityWrapper, weaponTitle, weaponStack, slot, triggerType, dualWield)) {
            if (triggerType.isSprintType()) getSkinHandler().tryUse(triggerType, entityWrapper, weaponTitle, weaponStack, slot);
            return;
        }

        // Reloading wasn't valid, try scoping
        if (scopeHandler.tryUse(entityWrapper, weaponTitle, weaponStack, slot, triggerType, dualWield)) {
            if (triggerType.isSprintType()) getSkinHandler().tryUse(triggerType, entityWrapper, weaponTitle, weaponStack, slot);
            return;
        }

        if (triggerType.isSprintType()) getSkinHandler().tryUse(triggerType, entityWrapper, weaponTitle, weaponStack, slot);

        // Scoping wasn't valid, try selective fire
        Configuration config = getConfigurations();
        Trigger selectiveFireTrigger = config.getObject(weaponTitle + ".Shoot.Selective_Fire.Trigger", Trigger.class);
        if (selectiveFireTrigger != null && selectiveFireTrigger.check(triggerType, slot, entityWrapper)) {
            boolean hasBurst = config.getInt(weaponTitle + ".Shoot.Burst.Shots_Per_Burst") != 0 && config.getInt(weaponTitle + ".Shoot.Burst.Ticks_Between_Each_Shot") != 0;
            boolean hasAuto = config.getInt(weaponTitle + ".Shoot.Fully_Automatic_Shots_Per_Second") != 0;

            // Order is
            // 1) Single
            // 2) Burst
            // 3) Auto
            if (!CustomTag.SELECTIVE_FIRE.hasInteger(weaponStack)) {
                if (hasBurst) {
                    CustomTag.SELECTIVE_FIRE.setInteger(weaponStack, BURST.getId());
                } else if (hasAuto) {
                    CustomTag.SELECTIVE_FIRE.setInteger(weaponStack, AUTO.getId());
                }
            } else {
                int currentSelectiveFire = CustomTag.SELECTIVE_FIRE.getInteger(weaponStack);
                switch (currentSelectiveFire) {
                    case (1): // 1 = burst, can't use SelectiveFireState.BURST.getId() here
                        if (hasAuto) {
                            CustomTag.SELECTIVE_FIRE.setInteger(weaponStack, AUTO.getId());
                        } else {
                            CustomTag.SELECTIVE_FIRE.setInteger(weaponStack, SINGLE.getId());
                        }
                        break;
                    case (2): // 2 = auto, can't use SelectiveFireState.AUTO.getId() here
                        CustomTag.SELECTIVE_FIRE.setInteger(weaponStack, SINGLE.getId());
                        break;
                    default:
                        if (hasBurst) {
                            CustomTag.SELECTIVE_FIRE.setInteger(weaponStack, BURST.getId());
                        } else if (hasAuto) {
                            CustomTag.SELECTIVE_FIRE.setInteger(weaponStack, AUTO.getId());
                        }
                        break;
                }
            }

            Mechanics selectiveFireMechanics = config.getObject(weaponTitle + ".Shoot.Selective_Fire", Mechanics.class);
            if (selectiveFireMechanics != null) selectiveFireMechanics.use(new CastData(entityWrapper, weaponTitle, weaponStack));

            WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
            if (weaponInfoDisplay != null) weaponInfoDisplay.send((IPlayerWrapper) entityWrapper, weaponTitle, weaponStack);

            entityWrapper.getMainHandData().cancelTasks();
            entityWrapper.getOffHandData().cancelTasks();
            return;
        }

        // Selective fire wasn't valid, try ammo type switch
        Trigger ammoTypeSwitchTrigger = config.getObject(weaponTitle + ".Reload.Ammo.Ammo_Type_Switch.Trigger", Trigger.class);
        if (ammoTypeSwitchTrigger != null && ammoTypeSwitchTrigger.check(triggerType, slot, entityWrapper)) {

            AmmoTypes ammoTypes = config.getObject(weaponTitle + ".Reload.Ammo.Ammo_Types", AmmoTypes.class);
            if (ammoTypes != null) {

                ammoTypes.updateToNextAmmoType(weaponStack);

                Mechanics ammoTypeSwitchMechanics = getConfigurations().getObject(weaponTitle + ".Reload.Ammo.Ammo_Type_Switch.Mechanics", Mechanics.class);
                if (ammoTypeSwitchMechanics != null) ammoTypeSwitchMechanics.use(new CastData(entityWrapper, weaponTitle, weaponStack));

                WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
                if (weaponInfoDisplay != null) weaponInfoDisplay.send((IPlayerWrapper) entityWrapper, weaponTitle, weaponStack);

                entityWrapper.getMainHandData().cancelTasks();
                entityWrapper.getOffHandData().cancelTasks();

                // Here has to be return if new triggers gets added
            }
        }
    }

    /**
     * @return the info handler
     */
    public InfoHandler getInfoHandler() {
        return infoHandler;
    }

    /**
     * @return the shoot handler
     */
    public ShootHandler getShootHandler() {
        return shootHandler;
    }

    /**
     * @return the reload handler
     */
    public ReloadHandler getReloadHandler() {
        return reloadHandler;
    }

    /**
     * @return the scope handler
     */
    public ScopeHandler getScopeHandler() {
        return scopeHandler;
    }

    /**
     * @return the damage handler
     */
    public DamageHandler getDamageHandler() {
        return damageHandler;
    }

    /**
     * @return the skin handler
     */
    public SkinHandler getSkinHandler() {
        return skinHandler;
    }
}