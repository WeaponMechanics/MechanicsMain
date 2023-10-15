package me.deecaad.weaponmechanics.weapon.placeholders;

import me.deecaad.core.placeholder.PlaceholderData;
import me.deecaad.core.placeholder.PlaceholderHandler;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmAction;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static me.deecaad.weaponmechanics.WeaponMechanics.getBasicConfigurations;
import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class PFirearmState extends PlaceholderHandler {

    public PFirearmState() {
        super("firearm_state");
    }

    @Nullable
    @Override
    public String onRequest(@NotNull PlaceholderData data) {
        if (data.item() == null || data.itemTitle() == null) return null;

        FirearmAction firearmAction = getConfigurations().getObject(data.itemTitle() + ".Firearm_Action", FirearmAction.class);

        // Simply don't show anything
        if (firearmAction == null) return "";

        FirearmState state = firearmAction.getState(data.item());

        return switch (state) {
            case OPEN -> getBasicConfigurations().getString("Placeholder_Symbols." + firearmAction.getFirearmType().name() + ".Open", " □");
            case CLOSE -> getBasicConfigurations().getString("Placeholder_Symbols." + firearmAction.getFirearmType().name() + ".Close", " ■");
            default -> "";
        };
    }
}