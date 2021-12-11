package me.deecaad.weaponmechanics.weapon.skin;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
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

        if (entityWrapper.isSprinting()) {
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
    public void validate(Configuration configuration, File file, ConfigurationSection configurationSection, String path) {

        // Convert the skins under "Skin" keyword to skin objects
        Skin skinSerializer = new Skin();
        for (String skinName : configurationSection.getConfigurationSection(path).getKeys(false)) {
            Skin skin = skinSerializer.serialize0(file, configurationSection, path + "." + skinName);
            if (skin == null) {
                continue;
            }
            configuration.set(path + "." + skinName, skin);
        }
    }
}
