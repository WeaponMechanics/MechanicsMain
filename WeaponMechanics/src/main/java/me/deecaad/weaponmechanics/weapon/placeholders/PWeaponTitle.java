package me.deecaad.weaponmechanics.weapon.placeholders;

import me.deecaad.core.placeholder.PlaceholderData;
import me.deecaad.core.placeholder.PlaceholderHandler;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;

public class PWeaponTitle extends PlaceholderHandler {

    public PWeaponTitle() {
        super("weapon_title");
    }

    @Nullable
    @Override
    public String onRequest(@NotNull PlaceholderData data) {
        return data.itemTitle();
    }
}
