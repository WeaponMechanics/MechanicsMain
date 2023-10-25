package me.deecaad.weaponmechanics.weapon.reload.ammo;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.SerializerOptionsException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.trigger.Trigger;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AmmoConfig implements Serializer<AmmoConfig> {

    private Mechanics outOfAmmoMechanics;
    private Trigger switchTrigger;
    private Mechanics switchMechanics;
    private List<Ammo> ammunitions;

    /**
     * Default constructor for serializer.
     */
    public AmmoConfig() {
    }

    public AmmoConfig(Mechanics outOfAmmoMechanics, Trigger switchTrigger, Mechanics switchMechanics, List<Ammo> ammunitions) {
        this.outOfAmmoMechanics = outOfAmmoMechanics;
        this.switchTrigger = switchTrigger;
        this.switchMechanics = switchMechanics;
        this.ammunitions = ammunitions;
    }

    public Mechanics getOutOfAmmoMechanics() {
        return outOfAmmoMechanics;
    }

    public Trigger getSwitchTrigger() {
        return switchTrigger;
    }

    public Mechanics getSwitchMechanics() {
        return switchMechanics;
    }

    public List<Ammo> getAmmunitions() {
        return ammunitions;
    }

    public int getCurrentAmmoIndex(ItemStack weapon) {
        int ammoIndex = CustomTag.AMMO_TYPE_INDEX.getInteger(weapon);
        // Clamping is really not needed, but it can happen when an existing
        // weapon uses an ammo that was removed from config.
        return NumberUtil.minMax(0, ammoIndex, ammunitions.size() - 1);
    }

    public void setCurrentAmmoIndex(ItemStack weapon, int index) {
        CustomTag.AMMO_TYPE_INDEX.setInteger(weapon, index);
    }

    public Ammo getCurrentAmmo(ItemStack weapon) {
        return ammunitions.get(getCurrentAmmoIndex(weapon));
    }

    public void updateToNextAmmo(ItemStack weapon) {
        int next = getCurrentAmmoIndex(weapon) + 1;
        if (next >= ammunitions.size()) next = 0;

        setCurrentAmmoIndex(weapon, next);
    }

    public boolean hasAmmo(String weaponTitle, ItemStack weapon, PlayerWrapper player) {
        int index = getCurrentAmmoIndex(weapon);
        if (ammunitions.get(index).getType().hasAmmo(player)) {
            return true;
        }

        // At this point, we know that we don't have ammo for the current
        // ammo type, BUT the player MIGHT have ammo for different types that
        // can be loaded into the gun. Of course, for that, we need the gun to
        // be empty. Then we search.
        int ammoLeft = CustomTag.AMMO_LEFT.getInteger(weapon);
        if (ammoLeft > 0) return false;

        for (int i = 0; i < ammunitions.size(); i++) {
            if (i == index) continue; // already checked for this ^^^

            if (!ammunitions.get(i).getType().hasAmmo(player)) continue;

            setCurrentAmmoIndex(weapon, i);
            if (switchMechanics != null)
                switchMechanics.use(new CastData(player.getPlayer(), weaponTitle, weapon));
            return true;
        }

        return false;
    }

    public int removeAmmo(ItemStack weapon, PlayerWrapper player, int amount, int maximumMagazineSize) {
        if (amount == 0) return 0;
        return getCurrentAmmo(weapon).getType().removeAmmo(weapon, player, amount, maximumMagazineSize);
    }

    public void giveAmmo(ItemStack weapon, PlayerWrapper playerWrapper, int amount, int maximumMagazineSize) {
        if (amount == 0) return;
        getCurrentAmmo(weapon).getType().giveAmmo(weapon, playerWrapper, amount, maximumMagazineSize);
    }

    public int getMaximumAmmo(ItemStack weapon, PlayerWrapper player, int maximumMagazineSize) {
        return getCurrentAmmo(weapon).getType().getMaximumAmmo(player, maximumMagazineSize);
    }

    @Override
    public String getKeyword() {
        return "Ammo";
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://cjcrafter.gitbook.io/weaponmechanics/weapon-modules/reload/ammo";
    }

    @NotNull
    @Override
    public AmmoConfig serialize(@NotNull SerializeData data) throws SerializerException {
        Mechanics mechanics = data.of("Out_Of_Ammo_Mechanics").serialize(Mechanics.class);
        Trigger switchTrigger = data.of("Ammo_Switch_Trigger").serialize(Trigger.class);
        Mechanics switchMechanics = data.of("Ammo_Switch_Mechanics").serialize(Mechanics.class);
        List<String> ammunitionStrings = data.of("Ammos").assertType(List.class).assertExists().get();

        List<Ammo> ammunitions = new ArrayList<>(ammunitionStrings.size());
        for (int i = 0; i < ammunitionStrings.size(); i++) {
            String ammoTitle = ammunitionStrings.get(i);
            Ammo ammo = AmmoRegistry.AMMO_REGISTRY.get(ammoTitle);

            // Make sure the ammo exists
            if (ammo == null) {
                throw new SerializerOptionsException(this, "Ammo", AmmoRegistry.AMMO_REGISTRY.getOptions(), ammoTitle, data.ofList("Ammos").getLocation(i));
            }

            ammunitions.add(ammo);
        }

        return new AmmoConfig(mechanics, switchTrigger, switchMechanics, ammunitions);
    }
}
