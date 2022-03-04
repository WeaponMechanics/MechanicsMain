package me.deecaad.weaponmechanics.weapon.skin;

import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.HandData;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class SkinHandler {

    private WeaponHandler weaponHandler;

    public SkinHandler(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    public boolean tryUse(EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot) {
        return tryUse(null, entityWrapper, weaponTitle, weaponStack, slot);
    }

    public boolean tryUse(TriggerType triggerType, EntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot) {
        HandData hand = slot == EquipmentSlot.HAND ? entityWrapper.getMainHandData() : entityWrapper.getOffHandData();
        Map<String, Skin> skins = getConfigurations().getObject(weaponTitle + ".Skin", Map.class);
        if (skins == null)
            return false;

        if (hand.getZoomData().isZooming()) {
            Skin zoomSkin = skins.get("Scope");

            Skin stackySkin = skins.get("Scope_" + hand.getZoomData().getZoomStacks());
            if (stackySkin != null) {
                zoomSkin = stackySkin;
            }

            if (zoomSkin != null) {
                zoomSkin.apply(weaponStack);
                return true;
            }
        }

        if (hand.isReloading()) {
            Skin reloadSkin = skins.get("Reload");
            if (reloadSkin != null) {
                reloadSkin.apply(weaponStack);
                return true;
            }
        }

        // Checks are like this due to when PlayerToggleSprintEvent is called player isn't yet actually sprinting
        // since the event is also cancellable. This ignores the cancelling of sprint event,
        // it doesn't do anything if its cancelled anyway :p
        if (triggerType == TriggerType.START_SPRINT || triggerType == null && entityWrapper.isSprinting()) {
            Skin sprintSkin = skins.get("Sprint");
            if (sprintSkin != null) {
                sprintSkin.apply(weaponStack);
                return true;
            }
        }

        Skin defaultSkin = skins.get("Default");
        if (defaultSkin != null) {
            defaultSkin.apply(weaponStack);
            return true;
        }

        return false;
    }
}
