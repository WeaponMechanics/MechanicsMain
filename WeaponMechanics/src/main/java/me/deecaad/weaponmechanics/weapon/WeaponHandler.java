package me.deecaad.weaponmechanics.weapon;

import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.listeners.trigger.TriggerPlayerListeners;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.damage.DamageHandler;
import me.deecaad.weaponmechanics.weapon.info.InfoHandler;
import me.deecaad.weaponmechanics.weapon.info.WeaponInfoDisplay;
import me.deecaad.weaponmechanics.weapon.melee.MeleeHandler;
import me.deecaad.weaponmechanics.weapon.reload.ReloadHandler;
import me.deecaad.weaponmechanics.weapon.reload.ammo.AmmoTypes;
import me.deecaad.weaponmechanics.weapon.scope.ScopeHandler;
import me.deecaad.weaponmechanics.weapon.shoot.SelectiveFireState;
import me.deecaad.weaponmechanics.weapon.shoot.ShootHandler;
import me.deecaad.weaponmechanics.weapon.skin.SkinHandler;
import me.deecaad.weaponmechanics.weapon.stats.StatsHandler;
import me.deecaad.weaponmechanics.weapon.trigger.Trigger;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

import static me.deecaad.weaponmechanics.WeaponMechanics.*;
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
    private final MeleeHandler meleeHandler;
    private final StatsHandler statsHandler;

    public WeaponHandler() {
        infoHandler = new InfoHandler(this);
        shootHandler = new ShootHandler(this);
        reloadHandler = new ReloadHandler(this);
        scopeHandler = new ScopeHandler(this);
        damageHandler = new DamageHandler(this);
        skinHandler = new SkinHandler(this);
        meleeHandler = new MeleeHandler(this);
        statsHandler = new StatsHandler(this);
    }

    /**
     * Checks main and off hand items if they're weapons and uses them based on trigger.
     * This is used with the exceptions off 
     * {@link TriggerPlayerListeners#dropItem(PlayerDropItemEvent)}, 
     * {@link TriggerPlayerListeners#interact(PlayerInteractEvent)}
     * and {@link TriggerPlayerListeners#swapHandItems(PlayerSwapHandItemsEvent)}.
     *
     * @param livingEntity the living entity which caused trigger
     * @param triggerType the trigger type
     * @param autoConvert whether or not the weapon items should be converted
     */
    public void useTrigger(LivingEntity livingEntity, TriggerType triggerType, boolean autoConvert) {

        // This because for all players this should be always nonnull
        // -> No auto add true to deny entity wrappers being added for unnecessary entities (they can also trigger certain
        EntityWrapper entityWrapper = getEntityWrapper(livingEntity, true);
        if (entityWrapper == null) return;

        if (livingEntity.getType() == EntityType.PLAYER && ((Player) livingEntity).getGameMode() == GameMode.SPECTATOR) return;

        EntityEquipment entityEquipment = livingEntity.getEquipment();
        if (entityEquipment == null) return;

        ItemStack mainStack = entityEquipment.getItemInMainHand();
        String mainWeapon = infoHandler.getWeaponTitle(mainStack, autoConvert);

        ItemStack offStack = entityEquipment.getItemInOffHand();
        String offWeapon = infoHandler.getWeaponTitle(offStack, autoConvert);

        if (mainWeapon == null && offWeapon == null) return;

        if (infoHandler.denyDualWielding(triggerType, livingEntity.getType() == EntityType.PLAYER ? (Player) livingEntity : null, mainWeapon, offWeapon)) return;

        boolean dualWield = mainWeapon != null && offWeapon != null;

        if (mainWeapon != null) tryUses(entityWrapper, mainWeapon, mainStack, EquipmentSlot.HAND, triggerType, dualWield, null);

        if (offWeapon != null) tryUses(entityWrapper, offWeapon, offStack, EquipmentSlot.OFF_HAND, triggerType, dualWield, null);
    }

    /**
     * Tries all uses in this exact order
     * <pre>{@code
     * 1) Shoot
     * 2) Reload
     * 3) Scope
     * 4) Selective fire
     * 5) Ammo type switch
     * }</pre>
     *
     * @param entityWrapper the entity which caused trigger
     * @param weaponTitle the weapon title involved
     * @param weaponStack the weapon stack involved
     * @param slot the weapon slot used
     * @param triggerType the trigger which caused this
     * @param dualWield whether this was dual wield
     * @param victim if there is known victim
     */
    public void tryUses(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, TriggerType triggerType, boolean dualWield, @Nullable LivingEntity victim) {
        if (!weaponStack.hasItemMeta()) return;

        // Try shooting (and melee), then reloading, then scoping
        if (shootHandler.tryUse(entityWrapper, weaponTitle, weaponStack, slot, triggerType, dualWield, victim)
                || reloadHandler.tryUse(entityWrapper, weaponTitle, weaponStack, slot, triggerType, dualWield)
                || scopeHandler.tryUse(entityWrapper, weaponTitle, weaponStack, slot, triggerType, dualWield)) {
            getSkinHandler().tryUse(triggerType, entityWrapper, weaponTitle, weaponStack, slot);
            return;
        }

        getSkinHandler().tryUse(triggerType, entityWrapper, weaponTitle, weaponStack, slot);

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
            // TODO simplify logic
            if (!CustomTag.SELECTIVE_FIRE.hasInteger(weaponStack)) {
                if (hasBurst) {
                    SelectiveFireState.setState(entityWrapper, weaponTitle, weaponStack, SINGLE, BURST);
                } else if (hasAuto) {
                    SelectiveFireState.setState(entityWrapper, weaponTitle, weaponStack, SINGLE, AUTO);
                }
            } else {
                int currentSelectiveFire = CustomTag.SELECTIVE_FIRE.getInteger(weaponStack);
                switch (currentSelectiveFire) {
                    case (1): // 1 = burst, can't use SelectiveFireState.BURST.getId() here
                        if (hasAuto) {
                            SelectiveFireState.setState(entityWrapper, weaponTitle, weaponStack, BURST, AUTO);
                        } else {
                            SelectiveFireState.setState(entityWrapper, weaponTitle, weaponStack, BURST, SINGLE);
                        }
                        break;
                    case (2): // 2 = auto, can't use SelectiveFireState.AUTO.getId() here
                        SelectiveFireState.setState(entityWrapper, weaponTitle, weaponStack, AUTO, SINGLE);
                        break;
                    default:
                        if (hasBurst) {
                            SelectiveFireState.setState(entityWrapper, weaponTitle, weaponStack, SINGLE, BURST);
                        } else if (hasAuto) {
                            SelectiveFireState.setState(entityWrapper, weaponTitle, weaponStack, SINGLE, AUTO);
                        }
                        break;
                }
            }

            Mechanics selectiveFireMechanics = config.getObject(weaponTitle + ".Shoot.Selective_Fire.Mechanics", Mechanics.class);
            if (selectiveFireMechanics != null) selectiveFireMechanics.use(new CastData(entityWrapper, weaponTitle, weaponStack));

            WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
            if (weaponInfoDisplay != null) weaponInfoDisplay.send((PlayerWrapper) entityWrapper, slot);

            entityWrapper.getMainHandData().cancelTasks();
            entityWrapper.getOffHandData().cancelTasks();
            return;
        }

        // Selective fire wasn't valid, try ammo type switch
        Trigger ammoTypeSwitchTrigger = config.getObject(weaponTitle + ".Reload.Ammo.Ammo_Type_Switch.Trigger", Trigger.class);
        if (ammoTypeSwitchTrigger != null && entityWrapper instanceof PlayerWrapper && ammoTypeSwitchTrigger.check(triggerType, slot, entityWrapper)) {

            AmmoTypes ammoTypes = config.getObject(weaponTitle + ".Reload.Ammo.Ammo_Types", AmmoTypes.class);
            if (ammoTypes != null) {

                // First empty the current ammo
                int ammoLeft = CustomTag.AMMO_LEFT.getInteger(weaponStack);
                if (ammoLeft > 0) {
                    ammoTypes.giveAmmo(weaponStack, (PlayerWrapper) entityWrapper, ammoLeft, config.getInt(weaponTitle + ".Reload.Magazine_Size"));
                    CustomTag.AMMO_LEFT.setInteger(weaponStack, 0);
                }

                // Then do the switch
                ammoTypes.updateToNextAmmoType(weaponStack);

                Mechanics ammoTypeSwitchMechanics = getConfigurations().getObject(weaponTitle + ".Reload.Ammo.Ammo_Type_Switch.Mechanics", Mechanics.class);
                if (ammoTypeSwitchMechanics != null) ammoTypeSwitchMechanics.use(new CastData(entityWrapper, weaponTitle, weaponStack));

                WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
                if (weaponInfoDisplay != null) weaponInfoDisplay.send((PlayerWrapper) entityWrapper, slot);

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

    /**
     * @return the melee handler
     */
    public MeleeHandler getMeleeHandler() {
        return meleeHandler;
    }

    /**
     * @return the stats handler
     */
    public StatsHandler getStatsHandler() {
        return statsHandler;
    }
}