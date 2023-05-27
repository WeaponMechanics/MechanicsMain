package me.deecaad.weaponmechanics.weapon.reload.ammo;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class AmmoTypes implements Serializer<AmmoTypes> {

    /**
     * Stores the names of currently registered ammo types, and their path
     * in config. This is used to determine if duplicate ammo names exist.
     * This can be cleared using {@link #clearRegistry()}.
     */
    private static final Map<String, String> REGISTERED_AMMO_TITLES = new HashMap<>();

    private List<IAmmoType> ammoTypes;

    /**
     * Empty constructor for serializer
     */
    public AmmoTypes() {
    }

    public AmmoTypes(List<IAmmoType> ammoTypes) {
        this.ammoTypes = ammoTypes;
    }

    public List<IAmmoType> getAmmoTypes() {
        return ammoTypes;
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

    public boolean hasAmmo(String weaponTitle, ItemStack weaponStack, PlayerWrapper playerWrapper) {
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
            if (ammoTypeSwitchMechanics != null)
                ammoTypeSwitchMechanics.use(new CastData(playerWrapper.getPlayer(), weaponTitle, weaponStack));
            return true;
        }
        return false;
    }

    public int removeAmmo(ItemStack weaponStack, PlayerWrapper playerWrapper, int amount, int maximumMagazineSize) {
        if (amount == 0) return 0;
        return ammoTypes.get(getCurrentAmmoIndex(weaponStack)).removeAmmo(weaponStack, playerWrapper, amount, maximumMagazineSize);

        // No need to try switching since at this point it's high unlikely that any ammo can't be
        // removed from player since hasAmmo(String, ItemStack, IPlayerWrapper) is called before this
        // which also handles the automatic switch
    }

    public void giveAmmo(ItemStack weaponStack, PlayerWrapper playerWrapper, int amount, int maximumMagazineSize) {
        if (amount == 0) return;
        ammoTypes.get(getCurrentAmmoIndex(weaponStack)).giveAmmo(weaponStack, playerWrapper, amount, maximumMagazineSize);

        // No need to try switching since this will simply give amount of current ammo back
    }

    public int getMaximumAmmo(ItemStack weaponStack, PlayerWrapper playerWrapper, int maximumMagazineSize) {
        // No need to try switching since this will simply maximum amount of current ammo
        return ammoTypes.get(getCurrentAmmoIndex(weaponStack)).getMaximumAmmo(playerWrapper, maximumMagazineSize);
    }

    @Override
    public String getKeyword() {
        return "Ammo_Types";
    }

    @Override
    @Nonnull
    public AmmoTypes serialize(SerializeData data) throws SerializerException {

        List<IAmmoType> ammoTypes = new ArrayList<>();
        ConfigurationSection config = data.of().assertExists().get();
        for (String ammoName : config.getKeys(false)) {

            // We have to check if an ammo-title with this name has already
            // been registered. Duplicates are not allowed.
            if (REGISTERED_AMMO_TITLES.containsKey(ammoName)) {
                throw data.exception(null, "Found duplicate ammo name",
                        SerializerException.forValue(ammoName),
                        "Instead of using a duplicate ammo names, try using something more specific, like 'AK-47_Ammo_1'",
                        "If you want to re-use ammo in each weapon, use the 'server > plugins > WeaponMechanics > ammo' folder",
                        "Ammo Tutorial: https://www.youtube.com/watch?v=eJwB0G1a4cE",
                        "Original Ammo is stored at: " + REGISTERED_AMMO_TITLES.get(ammoName));
            }

            // Store the location of the ammo key, in case we find duplicates
            REGISTERED_AMMO_TITLES.put(ammoName, data.of(ammoName).getLocation());

            SerializeData move = data.move(ammoName);

            String symbol = move.of("Symbol").assertType(String.class).get(null);

            // Experience
            int experienceAsAmmoCost = move.of("Experience_As_Ammo_Cost").assertPositive().getInt(-1);
            if (experienceAsAmmoCost != -1) {
                ammoTypes.add(new ExperienceAmmo(ammoName, symbol, experienceAsAmmoCost));
                continue;
            }

            // Money
            int moneyAsAmmoCost = move.of("Money_As_Ammo_Cost").assertPositive().getInt(-1);
            if (moneyAsAmmoCost != -1) {
                ammoTypes.add(new MoneyAmmo(ammoName, symbol, moneyAsAmmoCost));
                continue;
            }

            // Item
            ItemStack bulletItem = null;
            ItemStack magazineItem = null;

            // Items with NBT tags added have to be serialized in a special order,
            // otherwise the crafted item will be missing the NBT tag.
            if (move.has("Item_Ammo.Bullet_Item")) {
                Map<String, Object> tags = Map.of(CustomTag.AMMO_TITLE.getKey(), ammoName);
                bulletItem = new ItemSerializer().serializeWithTags(move.move("Item_Ammo.Bullet_Item"), tags);
            }

            // Items with NBT tags added have to be serialized in a special order,
            // otherwise the crafted item will be missing the NBT tag.
            if (move.has("Item_Ammo.Magazine_Item")) {
                Map<String, Object> tags = Map.of(CustomTag.AMMO_TITLE.getKey(), ammoName, CustomTag.AMMO_MAGAZINE.getKey(), 1);
                magazineItem = new ItemSerializer().serializeWithTags(move.move("Item_Ammo.Magazine_Item"), tags);
            }

            if (magazineItem == null && bulletItem == null) {
                throw move.exception(null, "Tried to use ammo without any options? You should use at least one of the ammo types!");
            }

            AmmoConverter ammoConverter = (AmmoConverter) move.of("Item_Ammo.Ammo_Converter_Check").serialize(new AmmoConverter());
            ammoTypes.add(new ItemAmmo(ammoName, symbol, bulletItem, magazineItem, ammoConverter));
        }
        return new AmmoTypes(ammoTypes);
    }

    public static void clearRegistry() {
        REGISTERED_AMMO_TITLES.clear();
    }
}
