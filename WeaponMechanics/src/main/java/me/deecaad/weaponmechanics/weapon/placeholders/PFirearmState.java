package me.deecaad.weaponmechanics.weapon.placeholders;

import me.deecaad.core.placeholder.PlaceholderHandler;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmAction;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

import static me.deecaad.weaponmechanics.WeaponMechanics.getBasicConfigurations;
import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class PFirearmState extends PlaceholderHandler {

    public PFirearmState() {
        super("%firearm-state%");
    }

    @Nullable
    @Override
    public String onRequest(@Nullable Player player, @Nullable ItemStack itemStack, @Nullable String weaponTitle, @Nullable EquipmentSlot slot) {
        if (itemStack == null || weaponTitle == null) return null;

        FirearmAction firearmAction = getConfigurations().getObject(weaponTitle + ".Firearm_Action", FirearmAction.class);
        if (firearmAction == null) return null;

        FirearmState state = firearmAction.getState(itemStack);

        switch (state) {
            case RELOAD_OPEN:
            case SHOOT_OPEN:
            case RELOAD:
                return getBasicConfigurations().getString("Placeholder_Symbols." + firearmAction.getFirearmType().name() + ".Open");
            case RELOAD_CLOSE:
            case SHOOT_CLOSE:
                return getBasicConfigurations().getString("Placeholder_Symbols." + firearmAction.getFirearmType().name() + ".Close");
            default:
                return "";
        }
    }
}