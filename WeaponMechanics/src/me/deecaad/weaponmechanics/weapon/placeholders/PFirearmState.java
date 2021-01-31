package me.deecaad.weaponmechanics.weapon.placeholders;

import me.deecaad.core.placeholder.PlaceholderHandler;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmAction;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

import static me.deecaad.weaponmechanics.WeaponMechanics.getBasicConfigurations;

public class PFirearmState extends PlaceholderHandler {

    public PFirearmState() {
        super("%firearm-state%");
    }

    @Nullable
    @Override
    public String onRequest(@Nullable Player player, @Nullable ItemStack itemStack, @Nullable String weaponTitle) {
        if (itemStack == null || weaponTitle == null) return null;

        FirearmAction firearmAction = getBasicConfigurations().getObject(weaponTitle + ".Firearm_Action", FirearmAction.class);
        if (firearmAction == null) return null;

        FirearmState state = firearmAction.getState(itemStack);

        switch (state) {
            case READY:
                return getBasicConfigurations().getString("Placeholder_Symbols." + state.name() + ".Ready");
            case RELOAD_OPEN:
            case SHOOT_OPEN:
            case RELOAD:
                return getBasicConfigurations().getString("Placeholder_Symbols." + state.name() + ".Open");
            case RELOAD_CLOSE:
            case SHOOT_CLOSE:
                return getBasicConfigurations().getString("Placeholder_Symbols." + state.name() + ".Close");
            default:
                return "";
        }
    }
}