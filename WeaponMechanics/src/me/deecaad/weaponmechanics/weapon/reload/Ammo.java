package me.deecaad.weaponmechanics.weapon.reload;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtils;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.utils.TagHelper;
import me.deecaad.weaponmechanics.weapon.info.WeaponConverter;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public class Ammo implements Serializer<Ammo> {

    private Mechanics outOfAmmo;
    private IAmmoType ammoType;

    /**
     * Empty constructor to be used as serializer
     */
    public Ammo() { }

    public Ammo(Mechanics outOfAmmo, IAmmoType ammoType) {
        this.outOfAmmo = outOfAmmo;
        this.ammoType = ammoType;
    }

    public void useOutOfAmmo(CastData castData) {
        if (outOfAmmo != null) outOfAmmo.use(castData);
    }

    /**
     * @param entityWrapper the entity
     * @return whether entity has at least 1 ammo
     */
    public boolean hasAmmo(IEntityWrapper entityWrapper) {
        return ammoType.hasAmmo(entityWrapper);
    }

    /**
     * @param entityWrapper the entity
     * @param magazineSize the weapon's full magazine size
     * @return the amount of this type ammo entity currently has
     */
    public int getAmount(IEntityWrapper entityWrapper, int magazineSize) {
        return ammoType.getAmount(entityWrapper, magazineSize);
    }

    /**
     * @param entityWrapper the entity
     * @param amount the amount to remove ammo
     * @param magazineSize the weapon's full magazine size
     * @return the amount of ammo that was removed from entity
     */
    public int remove(IEntityWrapper entityWrapper, int amount, int magazineSize) {
        return ammoType.remove(entityWrapper, amount, magazineSize);
    }

    /**
     * @param entityWrapper the entity
     * @param amount the amount of ammo to give for entity
     * @param magazineSize the weapon's full magazine size
     */
    public void give(IEntityWrapper entityWrapper, int amount, int magazineSize) {
        ammoType.give(entityWrapper, amount, magazineSize);
    }

    /**
     * @return whether this ammo uses item ammo type
     */
    public boolean isItemMagazineAmmo() {
        return ammoType instanceof ItemAmmo && ((ItemAmmo) ammoType).useMagazine();
    }

    @Override
    public String getKeyword() {
        return "Ammo";
    }

    @Override
    public Ammo serialize(File file, ConfigurationSection configurationSection, String path) {

        Mechanics outOfAmmo = new Mechanics().serialize(file, configurationSection, path);

        int expCost = configurationSection.getInt(path + ".Use_Exp_As_Ammo.Exp_Cost", -1);
        if (expCost != -1) {
            return new Ammo(outOfAmmo, new ExpAmmo(expCost));
        }

        double moneyCost = configurationSection.getDouble(path + ".Use_Money_As_Ammo.Money_Cost", -1);
        if (moneyCost != -1) {
            return new Ammo(outOfAmmo, new EconomyAmmo(moneyCost));
        }

        String ammoName = configurationSection.getString(path + ".Use_Item_As_Ammo.Ammo_Name");
        if (ammoName == null) return null;

        ItemStack magazineItem = new ItemSerializer().serialize(file, configurationSection, path + ".Use_Item_As_Ammo.Magazine.Item");
        Mechanics notSameAmmoName = null, magazineAlreadyFull = null, magazineFilled = null;
        if (magazineItem != null) {
            notSameAmmoName = new Mechanics().serialize(file, configurationSection, path + ".Use_Item_As_Ammo.Magazine.Not_Able_To_Fill.Not_Same_Ammo_Name");
            magazineAlreadyFull = new Mechanics().serialize(file, configurationSection, path + ".Use_Item_As_Ammo.Magazine.Not_Able_To_Fill.Magazine_Already_Full");
            magazineFilled = new Mechanics().serialize(file, configurationSection, path + ".Use_Item_As_Ammo.Magazine.Magazine_Filled");
        }

        ItemStack ammoItem = new ItemSerializer().serialize(file, configurationSection, path + ".Use_Item_As_Ammo.Ammo");
        if (ammoItem == null) {
            WeaponMechanics.debug.log(LogLevel.ERROR,
                    StringUtils.foundInvalid("ammo item"),
                    StringUtils.foundAt(file, path + ".Use_Item_As_Ammo.Ammo"),
                    "When using ammo you have to define at least ammo type.",
                    "Magazines also require this item so they can be filled.");
            return null;
        }

        magazineItem = TagHelper.setStringTag(magazineItem, CustomTag.ITEM_AMMO_NAME, ammoName);
        magazineItem = TagHelper.setIntegerTag(magazineItem, CustomTag.ITEM_AMMO_LEFT, 0);
        ammoItem = TagHelper.setStringTag(ammoItem, CustomTag.ITEM_AMMO_NAME, ammoName);

        WeaponConverter ammoConverter = new WeaponConverter().serialize(file, configurationSection, path + ".Use_Item_As_Ammo.Ammo_Converter_Check");

        // Add required TAGS for item ammo
        // If ItemAmmo -> ItemAmmo.register

        String weaponTitle = path.split("\\.")[0];
        int magazineSize = configurationSection.getInt(weaponTitle + ".Reload.Magazine_Size", -1);
        if (magazineSize == -1) {
            // Default to 30...
            magazineSize = 30;
        }

        ItemAmmo itemAmmo = new ItemAmmo(ammoName, magazineSize, magazineItem, notSameAmmoName, magazineAlreadyFull, magazineFilled, ammoItem, ammoConverter);

        if (!ItemAmmo.hasItemAmmo(itemAmmo)) {
            ItemAmmo.registerItemAmmo(itemAmmo);
        }

        return new Ammo(outOfAmmo, itemAmmo);
    }
}