package me.deecaad.weaponmechanics.weapon.placeholders;

import me.deecaad.core.placeholder.PlaceholderData;
import me.deecaad.core.placeholder.PlaceholderHandler;
import me.deecaad.weaponmechanics.utils.CustomTag;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;

public class PDurability extends PlaceholderHandler {

    public PDurability() {
        super("custom_durability");
    }

    @Nullable
    @Override
    public String onRequest(@NotNull PlaceholderData data) {
        if (data.item() == null) return null;

        return String.valueOf(CustomTag.DURABILITY.getInteger(data.item()));
    }
}
