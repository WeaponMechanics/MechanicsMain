package me.deecaad.weaponmechanics.weapon.skin;

import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponSkinEvent;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.HandData;
import org.bukkit.Bukkit;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class SkinHandler {

    private WeaponHandler weaponHandler;

    public SkinHandler(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    public boolean tryUse(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot) {
        return tryUse(null, entityWrapper, weaponTitle, weaponStack, slot, false);
    }

    public boolean tryUse(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, boolean forceDefault) {
        return tryUse(null, entityWrapper, weaponTitle, weaponStack, slot, forceDefault);
    }

    public boolean tryUse(TriggerType triggerType, EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot) {
        return tryUse(triggerType, entityWrapper, weaponTitle, weaponStack, slot, false);
    }

    public boolean tryUse(TriggerType triggerType, EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot, boolean forceDefault) {
        HandData hand = slot == EquipmentSlot.HAND ? entityWrapper.getMainHandData() : entityWrapper.getOffHandData();
        SkinList skins = getConfigurations().getObject(weaponTitle + ".Skin", SkinList.class);
        if (skins == null || !weaponStack.hasItemMeta())
            return false;

        WeaponSkinEvent event = new WeaponSkinEvent(weaponTitle, weaponStack, entityWrapper.getEntity(), slot, skins, triggerType, forceDefault);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return false;

        Skin skin = getSkin(skins, event.getSkin(), hand, weaponStack, triggerType, forceDefault);
        if (skin != null) {
            skin.apply(weaponStack);
            return true;
        }

        return false;
    }

    public Skin getSkin(SkinList skins, String skin, HandData hand, ItemStack weaponStack, TriggerType triggerType) {
        return getSkin(skins, skin, hand, weaponStack, triggerType, false);
    }

    public Skin getSkin(SkinList skins, String skin, HandData hand, ItemStack weaponStack, TriggerType triggerType, boolean forceDefault) {
        if (forceDefault) return skins.getSkin(skin, SkinList.SkinIdentifier.DEFAULT);

        Skin reloadSkin = skins.getSkin(skin, SkinList.SkinIdentifier.RELOAD);
        if ((!hand.isReloading() || reloadSkin == null) && CustomTag.AMMO_LEFT.getInteger(weaponStack) == 0) {
            Skin emptyAmmoSkin = skins.getSkin(skin, SkinList.SkinIdentifier.NO_AMMO);
            if (emptyAmmoSkin != null)
                return emptyAmmoSkin;
        }

        if (hand.getZoomData().isZooming()) {
            Skin zoomSkin = skins.getSkin(skin, SkinList.SkinIdentifier.SCOPE);

            Skin stackySkin = skins.getSkin(skin, new SkinList.SkinIdentifier("Scope_" + hand.getZoomData().getZoomStacks()));
            if (stackySkin != null)
                zoomSkin = stackySkin;

            if (zoomSkin != null)
                return zoomSkin;
        }

        if (hand.isReloading()) {
            if (reloadSkin != null)
                return reloadSkin;
        }

        // Checks are like this due to when PlayerToggleSprintEvent is called player isn't yet actually sprinting
        // since the event is also cancellable. This ignores the cancelling of sprint event,
        // it doesn't do anything if its cancelled anyway :p
        // + disable when dual wielding ++ don't even try when its END_SPRINT
        EntityWrapper entityWrapper = hand.getEntityWrapper();
        if (triggerType != TriggerType.END_SPRINT
                && (entityWrapper.isSprinting() || triggerType == TriggerType.START_SPRINT)
                && !entityWrapper.isDualWielding()) {

            Skin sprintSkin = skins.getSkin(skin, SkinList.SkinIdentifier.SPRINT);
            if (sprintSkin != null)
                return sprintSkin;
        }

        return skins.getSkin(skin, SkinList.SkinIdentifier.DEFAULT);
    }
}
