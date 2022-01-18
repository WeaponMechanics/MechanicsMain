package me.deecaad.weaponmechanics.weapon.skin;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.wrappers.HandData;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.io.File;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class SkinHandler implements IValidator {

    private WeaponHandler weaponHandler;

    public SkinHandler() { }

    public SkinHandler(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    public boolean tryUse(IEntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot) {
        return tryUse(null, entityWrapper, weaponTitle, weaponStack, slot);
    }

    public boolean tryUse(TriggerType triggerType, IEntityWrapper entityWrapper, String weaponTitle, ItemStack weaponStack, EquipmentSlot slot) {
        HandData hand = slot == EquipmentSlot.HAND ? entityWrapper.getMainHandData() : entityWrapper.getOffHandData();

        if (hand.getZoomData().isZooming()) {
            Skin zoomSkin = getConfigurations().getObject(weaponTitle + ".Skin.Scope", Skin.class);

            Skin stackySkin = getConfigurations().getObject(weaponTitle + ".Skin.Scope_" + hand.getZoomData().getZoomStacks(), Skin.class);
            if (stackySkin != null) {
                zoomSkin = stackySkin;
            }

            if (zoomSkin != null) {
                zoomSkin.apply(weaponStack);
                return true;
            }
        }

        if (hand.isReloading()) {
            Skin reloadSkin = getConfigurations().getObject(weaponTitle + ".Skin.Reload", Skin.class);
            if (reloadSkin != null) {
                reloadSkin.apply(weaponStack);
                return true;
            }
        }

        // Checks are like this due to when PlayerToggleSprintEvent is called player isn't yet actually sprinting
        // since the event is also cancellable. This ignores the cancelling of sprint event,
        // it doesn't do anything if its cancelled anyway :p
        if (triggerType == TriggerType.START_SPRINT || triggerType == null && entityWrapper.isSprinting()) {
            Skin sprintSkin = getConfigurations().getObject(weaponTitle + ".Skin.Sprint", Skin.class);
            if (sprintSkin != null) {
                sprintSkin.apply(weaponStack);
                return true;
            }
        }

        Skin defaultSkin = getConfigurations().getObject(weaponTitle + ".Skin.Default", Skin.class);
        if (defaultSkin != null) {
            defaultSkin.apply(weaponStack);
            return true;
        }

        return false;
    }

    @Override
    public String getKeyword() {
        return "Skin";
    }

    @Override
    public void validate(Configuration configuration, File file, ConfigurationSection configurationSection, String path) throws SerializerException {

        // Convert the skins under "Skin" keyword to skin objects
        Skin skinSerializer = new Skin();
        for (String skinName : configurationSection.getConfigurationSection(path).getKeys(false)) {
            Skin skin = skinSerializer.serialize(new SerializeData(null, file, path + "." + skinName, configurationSection));
            configuration.set(path + "." + skinName, skin);
        }
    }
}
