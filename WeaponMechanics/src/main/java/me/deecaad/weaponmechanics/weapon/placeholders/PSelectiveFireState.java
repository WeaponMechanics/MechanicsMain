package me.deecaad.weaponmechanics.weapon.placeholders;

import me.deecaad.core.placeholder.PlaceholderData;
import me.deecaad.core.placeholder.PlaceholderHandler;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.shoot.SelectiveFireState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static me.deecaad.weaponmechanics.WeaponMechanics.getBasicConfigurations;

public class PSelectiveFireState extends PlaceholderHandler {

    public PSelectiveFireState() {
        super("selective_fire_state");
    }

    @Nullable
    @Override
    public String onRequest(@NotNull PlaceholderData data) {
        if (data.item() == null) return null;

        int selectiveFireState = CustomTag.SELECTIVE_FIRE.getInteger(data.item());
        SelectiveFireState state = SelectiveFireState.getState(selectiveFireState);
        return getBasicConfigurations().getString("Placeholder_Symbols.Selective_Fire." + state.name());
    }
}