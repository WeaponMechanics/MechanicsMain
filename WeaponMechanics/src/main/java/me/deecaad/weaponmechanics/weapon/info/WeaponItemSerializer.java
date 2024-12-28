package me.deecaad.weaponmechanics.weapon.info;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.file.simple.StringSerializer;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.shoot.SelectiveFireState;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Simple class to handle weapon item serialization a bit differently.
 */
public class WeaponItemSerializer extends ItemSerializer {

    /**
     * Default constructor for serializer
     */
    public WeaponItemSerializer() {
    }

    @Override
    public String getKeyword() {
        return "Weapon_Item";
    }

    @Override
    @NotNull public ItemStack serialize(@NotNull SerializeData data) throws SerializerException {
        ItemStack weaponStack = super.serializeWithoutRecipe(data);

        // Crossbows are not allowed to be used as the weapon's type. WMC
        // supports "faking" the crossbow for other players. I added this check
        // since it is a commonly asked question, and this should save me some
        // time in the long run.
        if (weaponStack.getType().name().equals("CROSSBOW")) {
            throw data.exception("Type", "You cannot use 'CROSSBOW' as a WeaponMechanics weapon!",
                "YES! We know that you want weapons to be 'held up' like a minecraft crossbow",
                "Purchase WMC to 'fake' the crossbow animation for other players: https://www.spigotmc.org/resources/104539/");
        }

        String weaponTitle = data.getKey().split("\\.")[0];

        // Saving display name and lore for use in WeaponMechanicsPlus to auto update items
        Configuration config = WeaponMechanics.getConfigurations();
        String weaponDisplay = data.of("Name").getAdventure().orElse(null);
        if (weaponDisplay != null) {
            config.set(weaponTitle + ".Info.Weapon_Item.Display", weaponDisplay);
        }

        List<String> weaponLore = data.ofList("Lore")
            .addArgument(new StringSerializer())
            .assertList()
            .stream()
            .map(split -> (String) split.get(0).get())
            .toList();
        if (!weaponLore.isEmpty()) {
            config.set(weaponTitle + ".Info.Weapon_Item.Lore", weaponLore.stream()
                .map(line -> "<!italic>" + StringUtil.colorAdventure(line))
                .toList());
        }

        // Ensure the weapon title uses the correct format, mostly for other plugin compatibility
        Pattern pattern = Pattern.compile("[A-Za-z0-9_]+");
        if (!pattern.matcher(weaponTitle).matches()) {
            throw data.exception(null, "Weapon title must only contain letters, numbers, and underscores!",
                "For example, AK-47 is not allowed, but AK_47 is fine",
                "This is only for the weapon title (the name defined in config), NOT the display name of the weapon. The display can be whatever you want.",
                "Found weapon title: " + weaponTitle);
        }

        WeaponMechanics.getWeaponHandler().getInfoHandler().addWeapon(weaponTitle);

        int magazineSize = (Integer) data.getConfig().get(weaponTitle + ".Reload.Magazine_Size", -1);
        if (magazineSize != -1) {
            CustomTag.AMMO_LEFT.setInteger(weaponStack, magazineSize);
        }

        String defaultSelectiveFire = data.getConfig().getString(weaponTitle + ".Shoot.Selective_Fire.Default");
        if (defaultSelectiveFire != null) {

            try {
                SelectiveFireState state = SelectiveFireState.valueOf(defaultSelectiveFire);
                CustomTag.SELECTIVE_FIRE.setInteger(weaponStack, state.ordinal());
            } catch (IllegalArgumentException e) {
                throw SerializerException.builder()
                    .location(data.getFile(), weaponTitle + ".Shoot.Selective_Fire.Default", null)
                    .buildInvalidEnumOption(defaultSelectiveFire, SelectiveFireState.class);
            }
        }

        CustomTag.WEAPON_TITLE.setString(weaponStack, weaponTitle);
        weaponStack = super.serializeRecipe(data, weaponStack);
        return weaponStack;
    }
}