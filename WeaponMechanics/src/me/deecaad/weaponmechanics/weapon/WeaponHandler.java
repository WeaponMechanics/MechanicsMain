package me.deecaad.weaponmechanics.weapon;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.utils.TagHelper;
import me.deecaad.weaponmechanics.weapon.info.InfoHandler;
import me.deecaad.weaponmechanics.weapon.reload.ReloadHandler;
import me.deecaad.weaponmechanics.weapon.scope.ScopeHandler;
import me.deecaad.weaponmechanics.weapon.shoot.ShootHandler;
import me.deecaad.weaponmechanics.weapon.trigger.Trigger;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

import static me.deecaad.weaponmechanics.WeaponMechanics.*;

/**
 * Class to generally handle weapon functions
 */
public class WeaponHandler {

    private InfoHandler infoHandler;
    private ShootHandler shootHandler;
    private ReloadHandler reloadHandler;
    private ScopeHandler scopeHandler;

    public WeaponHandler() {
        infoHandler = new InfoHandler(this);
        shootHandler = new ShootHandler(this);
        reloadHandler = new ReloadHandler(this);
        scopeHandler = new ScopeHandler(this);
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

        boolean useOffHand = CompatibilityAPI.getVersion() >= 1.09;

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

        if (mainWeapon == null && offWeapon == null) return;

        // Only do dual wield check if server is 1.9 or newer
        if (useOffHand && infoHandler.denyDualWielding(triggerType, livingEntity.getType() == EntityType.PLAYER ? (Player) livingEntity : null, mainWeapon, offWeapon)) return;

        boolean dualWield = mainWeapon != null && offWeapon != null;

        if (mainWeapon != null) tryUses(entityWrapper, mainWeapon, mainStack, EquipmentSlot.HAND, triggerType, dualWield);

        // Off weapon is automatically null at this point if server is using 1.8
        if (offWeapon != null) tryUses(entityWrapper, offWeapon, offStack, EquipmentSlot.OFF_HAND, triggerType, dualWield);
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
        if (shootHandler.tryUse(entityWrapper, weaponTitle, weaponStack, slot, triggerType, dualWield)) return;

        // Shooting wasn't valid, try reloading
        if (reloadHandler.tryUse(entityWrapper, weaponTitle, weaponStack, slot, triggerType, dualWield)) return;

        // Reloading wasn't valid, try scoping
        if (scopeHandler.tryUse(entityWrapper, weaponTitle, weaponStack, slot, triggerType, dualWield)) return;

        // Scoping wasn't valid, try selective fire
        Configuration config = getConfigurations();
        Trigger selectiveFireTrigger = config.getObject(weaponTitle + ".Shoot.Selective_Fire.Trigger", Trigger.class);
        if (selectiveFireTrigger != null && selectiveFireTrigger.check(triggerType, slot, entityWrapper)) {
            boolean hasBurst = config.getInt(weaponTitle + ".Shoot.Burst.Shots_Per_Burst") != 0 && config.getInt(weaponTitle + ".Shoot.Burst.Ticks_Between_Each_Shot") != 0;
            boolean hasAuto = config.getInt(weaponTitle + ".Shoot.Fully_Automatic_Shots_Per_Second") != 0;

            // Order is basically
            // 1) Single
            // 2) Burst
            // 3) Auto

            String currentSelectiveFire = TagHelper.getStringTag(weaponStack, CustomTag.SELECTIVE_FIRE);
            if (currentSelectiveFire == null) {
                if (hasBurst) {
                    TagHelper.setStringTag(weaponStack, CustomTag.SELECTIVE_FIRE, "burst");
                } else if (hasAuto) {
                    TagHelper.setStringTag(weaponStack, CustomTag.SELECTIVE_FIRE, "auto");
                }
            } else {
                switch (currentSelectiveFire) {
                    case ("burst"):
                        if (hasAuto) {
                            TagHelper.setStringTag(weaponStack, CustomTag.SELECTIVE_FIRE, "auto");
                        } else {
                            TagHelper.setStringTag(weaponStack, CustomTag.SELECTIVE_FIRE, "single");
                        }
                        break;
                    case ("auto"):
                        TagHelper.setStringTag(weaponStack, CustomTag.SELECTIVE_FIRE, "single");
                        break;
                    default:
                        if (hasBurst) {
                            TagHelper.setStringTag(weaponStack, CustomTag.SELECTIVE_FIRE, "burst");
                        } else if (hasAuto) {
                            TagHelper.setStringTag(weaponStack, CustomTag.SELECTIVE_FIRE, "auto");
                        }
                        break;
                }
            }

            entityWrapper.getMainHandData().cancelTasks();
            entityWrapper.getOffHandData().cancelTasks();
        }
    }

    /**
     * @return the current info handler
     */
    public InfoHandler getInfoHandler() {
        return infoHandler;
    }

    /**
     * Sets new info handler for WeaponMechanics.
     * It is up to you how you use this. You can override all methods used by
     * default if you want to or simply some methods you want to modify some.
     *
     * WeaponMechanics doesn't notify you about changes in info handler code
     * so you will have to be careful when using this method. It is recommended
     * to use super.method() and after that add your new stuff you want to add, this
     * way the compatibility with WeaponMechanics should stay, but it is still not guaranteed compatibility!
     *
     * @param infoHandler the new info handler
     */
    public void setInfoHandler(@Nonnull InfoHandler infoHandler) {
        if (infoHandler == null) throw new NullPointerException("Someone tried to set null info handler...");
        this.infoHandler = infoHandler;
    }

    /**
     * @return the current shoot handler
     */
    public ShootHandler getShootHandler() {
        return shootHandler;
    }

    /**
     * Sets new shoot handler for WeaponMechanics.
     * It is up to you how you use this. You can override all methods used by
     * default if you want to or simply some methods you want to modify some.
     *
     * WeaponMechanics doesn't notify you about changes in shoot handler code
     * so you will have to be careful when using this method. It is recommended
     * to use super.method() and after that add your new stuff you want to add, this
     * way the compatibility with WeaponMechanics should stay, but it is still not guaranteed compatibility!
     *
     * @param shootHandler the new shoot handler
     */
    public void setShootHandler(@Nonnull ShootHandler shootHandler) {
        if (shootHandler == null) throw new NullPointerException("Someone tried to set null shoot handler...");
        this.shootHandler = shootHandler;
    }

    /**
     * @return the current reload handler
     */
    public ReloadHandler getReloadHandler() {
        return reloadHandler;
    }

    /**
     * Sets new reload handler for WeaponMechanics.
     * It is up to you how you use this. You can override all methods used by
     * default if you want to or simply some methods you want to modify some.
     *
     * WeaponMechanics doesn't notify you about changes in reload handler code
     * so you will have to be careful when using this method. It is recommended
     * to use super.method() and after that add your new stuff you want to add, this
     * way the compatibility with WeaponMechanics should stay, but it is still not guaranteed compatibility!
     *
     * @param reloadHandler the new reload handler
     */
    public void setReloadHandler(@Nonnull ReloadHandler reloadHandler) {
        if (reloadHandler == null) throw new NullPointerException("Someone tried to set null reload handler...");
        this.reloadHandler = reloadHandler;
    }

    /**
     * @return the current scope handler
     */
    public ScopeHandler getScopeHandler() {
        return scopeHandler;
    }

    /**
     * Sets new scope handler for WeaponMechanics.
     * It is up to you how you use this. You can override all methods used by
     * default if you want to or simply some methods you want to modify some.
     *
     * WeaponMechanics doesn't notify you about changes in scope handler code
     * so you will have to be careful when using this method. It is recommended
     * to use super.method() and after that add your new stuff you want to add, this
     * way the compatibility with WeaponMechanics should stay, but it is still not guaranteed compatibility!
     *
     * @param scopeHandler the new scope handler
     */
    public void setScopeHandler(@Nonnull ScopeHandler scopeHandler) {
        if (scopeHandler == null) throw new NullPointerException("Someone tried to set null scope handler...");
        this.scopeHandler = scopeHandler;
    }
}