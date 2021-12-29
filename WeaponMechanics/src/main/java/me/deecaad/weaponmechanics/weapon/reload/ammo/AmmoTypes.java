package me.deecaad.weaponmechanics.weapon.reload.ammo;

import me.deecaad.core.file.Serializer;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.io.File;
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

            // Empty the weapon before switching using old ammo type
            ammoTypes.get(index).giveAmmo(weaponStack, playerWrapper, ammoLeft);
            CustomTag.AMMO_LEFT.setInteger(weaponStack, 0);

            // Update the index automatically to use this new one
            setCurrentAmmoIndex(weaponStack, i);

            Mechanics ammoTypeSwitchMechanics = getConfigurations().getObject(weaponTitle + ".Reload.Ammo.Ammo_Type_Switch.Mechanics", Mechanics.class);
            if (ammoTypeSwitchMechanics != null) ammoTypeSwitchMechanics.use(new CastData(playerWrapper, weaponTitle, weaponStack));
            return true;
        }
        return false;
    }

    public int removeAmmo(ItemStack weaponStack, IPlayerWrapper playerWrapper, int amount) {
        return ammoTypes.get(getCurrentAmmoIndex(weaponStack)).removeAmmo(weaponStack, playerWrapper, amount);

        // No need to try switching since at this point it's high unlikely that any ammo can't be
        // removed from player since hasAmmo(String, ItemStack, IPlayerWrapper) is called before this
        // which also handles the automatic switch
    }

    public void giveAmmo(ItemStack weaponStack, IPlayerWrapper playerWrapper, int amount) {
        ammoTypes.get(getCurrentAmmoIndex(weaponStack)).giveAmmo(weaponStack, playerWrapper, amount);

        // No need to try switching since this will simply give amount of current ammo back
    }

    public int getMaximumAmmo(ItemStack weaponStack, IPlayerWrapper playerWrapper) {
        // No need to try switching since this will simply maximum amount of current ammo
        return ammoTypes.get(getCurrentAmmoIndex(weaponStack)).getMaximumAmmo(playerWrapper);
    }

    @Override
    public String getKeyword() {
        return "Ammo_Types";
    }

    @Override
    public AmmoTypes serialize(File file, ConfigurationSection configurationSection, String path) {
        return null;
    }
}
