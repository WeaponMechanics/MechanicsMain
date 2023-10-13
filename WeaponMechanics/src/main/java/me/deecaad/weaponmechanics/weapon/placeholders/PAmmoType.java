package me.deecaad.weaponmechanics.weapon.placeholders;

import me.deecaad.core.placeholder.PlaceholderData;
import me.deecaad.core.placeholder.PlaceholderHandler;
import me.deecaad.weaponmechanics.weapon.reload.ammo.AmmoConfig;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class PAmmoType extends PlaceholderHandler {

    public PAmmoType() {
        super("ammo_type");
    }

    @Nullable
    @Override
    public String onRequest(@NotNull PlaceholderData data) {
        if (data.item() == null || data.itemTitle() == null) return null;

        AmmoConfig ammoTypes = getConfigurations().getObject(data.itemTitle() + ".Reload.Ammo", AmmoConfig.class);

        // Simply don't show anything
        if (ammoTypes == null)
            return null;

        return ammoTypes.getCurrentAmmo(data.item()).getDisplay();
    }
}
