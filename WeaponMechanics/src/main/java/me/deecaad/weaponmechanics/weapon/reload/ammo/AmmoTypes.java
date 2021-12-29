package me.deecaad.weaponmechanics.weapon.reload.ammo;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class AmmoTypes implements Serializer<AmmoTypes> {

    private List<IAmmoType> ammoTypes;

    /**
     * Empty constructor to be used as serializer
     */
    public AmmoTypes() { }

    public AmmoTypes(List<IAmmoType> ammoTypes) {
        this.ammoTypes = ammoTypes;
    }

    public String getCurrentAmmoName(ItemStack weaponStack) {
        return ammoTypes.get(CustomTag.AMMO_TYPE_INDEX.getInteger(weaponStack)).getAmmoName();
    }

    public String getCurrentAmmoSymbol(ItemStack weaponStack) {
        return ammoTypes.get(CustomTag.AMMO_TYPE_INDEX.getInteger(weaponStack)).getSymbol();
    }

    public int getCurrentAmmoIndex(ItemStack weaponStack) {
        return CustomTag.AMMO_TYPE_INDEX.getInteger(weaponStack);
    }

    public void setCurrentAmmoIndex(ItemStack weaponStack, int index) {
        CustomTag.AMMO_TYPE_INDEX.setInteger(weaponStack, index);
    }

    public void updateToNextAmmoType(ItemStack weaponStack) {
        int nextIndex = getCurrentAmmoIndex(weaponStack) + 1;

        if (nextIndex >= ammoTypes.size()) nextIndex = 0;

        setCurrentAmmoIndex(weaponStack, nextIndex);
    }

    public boolean hasAmmo(String weaponTitle, ItemStack weaponStack, IPlayerWrapper playerWrapper) {
        int index = getCurrentAmmoIndex(weaponStack);
        if (ammoTypes.get(index).hasAmmo(playerWrapper)) {
            return true;
        }

        if (ammoTypes.size() == 1) return false;

        int ammoLeft = CustomTag.AMMO_LEFT.getInteger(weaponStack);

        // If player didn't have ammo using current ammo type, but clip still has ammo left
        // don't try to do the automatic switch and simply return false to indicate that
        // player is out of ammo
        if (ammoLeft > 0) return false;

        // Check from top to bottom for other ammo types
        for (int i = 0; i < ammoTypes.size(); ++i) {
            if (i == index) continue; // Don't try checking for that ammo type anymore

            if (!ammoTypes.get(i).hasAmmo(playerWrapper)) continue;

            // Update the index automatically to use this new one
            setCurrentAmmoIndex(weaponStack, i);

            Mechanics ammoTypeSwitchMechanics = getConfigurations().getObject(weaponTitle + ".Reload.Ammo.Ammo_Type_Switch.Mechanics", Mechanics.class);
            if (ammoTypeSwitchMechanics != null) ammoTypeSwitchMechanics.use(new CastData(playerWrapper, weaponTitle, weaponStack));
            return true;
        }
        return false;
    }

    public int removeAmmo(ItemStack weaponStack, IPlayerWrapper playerWrapper, int amount, int maximumMagazineSize) {
        if (amount == 0) return 0;
        return ammoTypes.get(getCurrentAmmoIndex(weaponStack)).removeAmmo(weaponStack, playerWrapper, amount, maximumMagazineSize);

        // No need to try switching since at this point it's high unlikely that any ammo can't be
        // removed from player since hasAmmo(String, ItemStack, IPlayerWrapper) is called before this
        // which also handles the automatic switch
    }

    public void giveAmmo(ItemStack weaponStack, IPlayerWrapper playerWrapper, int amount, int maximumMagazineSize) {
        if (amount == 0) return;
        ammoTypes.get(getCurrentAmmoIndex(weaponStack)).giveAmmo(weaponStack, playerWrapper, amount, maximumMagazineSize);

        // No need to try switching since this will simply give amount of current ammo back
    }

    public int getMaximumAmmo(ItemStack weaponStack, IPlayerWrapper playerWrapper, int maximumMagazineSize) {
        // No need to try switching since this will simply maximum amount of current ammo
        return ammoTypes.get(getCurrentAmmoIndex(weaponStack)).getMaximumAmmo(playerWrapper, maximumMagazineSize);
    }

    @Override
    public String getKeyword() {
        return "Ammo_Types";
    }

    @Override
    public AmmoTypes serialize(File file, ConfigurationSection configurationSection, String path) {
        List<IAmmoType> ammoTypes = new ArrayList<>();
        for (String ammoName : configurationSection.getConfigurationSection(path).getKeys(false)) {
            String nowPath = path + "." + ammoName;
            String symbol = configurationSection.getString(nowPath + ".Symbol");

            // Experience
            int experienceAsAmmoCost = configurationSection.getInt(nowPath + ".Experience_As_Ammo_Cost", -1);
            if (experienceAsAmmoCost != -1) {
                ammoTypes.add(new ExperienceAmmo(ammoName, symbol, experienceAsAmmoCost));
                continue;
            }

            // Money
            int moneyAsAmmoCost = configurationSection.getInt(nowPath + ".Money_As_Ammo_Cost", -1);
            if (moneyAsAmmoCost != -1) {
                ammoTypes.add(new MoneyAmmo(ammoName, symbol, moneyAsAmmoCost));
                continue;
            }

            // Item
            ItemStack bulletItem = new ItemSerializer().serializeWithoutRecipe(file, configurationSection, nowPath + ".Item_Ammo.Bullet_Item");
            ItemStack magazineItem = new ItemSerializer().serializeWithoutRecipe(file, configurationSection, nowPath + ".Item_Ammo.Magazine_Item");

            if (magazineItem == null && bulletItem == null) continue;

            if (bulletItem != null) {
                CustomTag.AMMO_NAME.setString(bulletItem, ammoName);

                bulletItem = new ItemSerializer().serializeRecipe(file, configurationSection, nowPath + ".Item_Ammo.Bullet_Item", bulletItem);
            }

            // Not else if since both of these can be used at same time
            if (magazineItem != null) {
                CustomTag.AMMO_NAME.setString(magazineItem, ammoName);

                // Set to indicate that this item is magazine
                CustomTag.AMMO_MAGAZINE.setInteger(magazineItem, 1);

                magazineItem = new ItemSerializer().serializeRecipe(file, configurationSection, nowPath + ".Item_Ammo.Magazine_Item", magazineItem);
            }

            AmmoConverter ammoConverter = new AmmoConverter().serialize(file, configurationSection, nowPath + ".Item_Ammo.Ammo_Converter_Check");
            ammoTypes.add(new ItemAmmo(ammoName, symbol, bulletItem, magazineItem, ammoConverter));
        }
        return ammoTypes.isEmpty() ? null : new AmmoTypes(ammoTypes);
    }
}
