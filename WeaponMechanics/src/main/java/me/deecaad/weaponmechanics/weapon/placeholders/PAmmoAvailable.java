package me.deecaad.weaponmechanics.weapon.placeholders;

import me.deecaad.core.placeholder.PlaceholderData;
import me.deecaad.core.placeholder.PlaceholderHandler;
import me.deecaad.weaponmechanics.weapon.reload.ammo.AmmoTypes;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;
import static me.deecaad.weaponmechanics.WeaponMechanics.getPlayerWrapper;

public class PAmmoAvailable extends PlaceholderHandler {

    public PAmmoAvailable() {
        super("ammo_available");
    }

    @Nullable
    @Override
    public String onRequest(@NotNull PlaceholderData data) {
        if (data.item() == null || data.itemTitle() == null) return null;

        AmmoTypes ammoTypes = getConfigurations().getObject(data.itemTitle() + ".Reload.Ammo.Ammo_Types", AmmoTypes.class);

        if (ammoTypes == null)
            return null;

        return String.valueOf(ammoTypes.getMaximumAmmo(data.item(), getPlayerWrapper(data.player()), getConfigurations().getInt(data.itemTitle() + ".Reload.Magazine_Size")));
    }
}
