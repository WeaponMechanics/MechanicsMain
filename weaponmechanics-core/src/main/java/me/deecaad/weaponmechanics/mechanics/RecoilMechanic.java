package me.deecaad.weaponmechanics.mechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.verify.ConfigSchema;
import me.deecaad.core.mechanics.defaultmechanics.Mechanic;
import me.deecaad.core.mechanics.scope.CastScope;
import me.deecaad.core.mechanics.scope.Target;
import me.deecaad.core.mechanics.scope.TargetKind;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.shoot.recoil.RecoilProfile;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

/**
 * Applies screen recoil to the shooter, letting a config author kick the player's view from any
 * mechanic line (not just on shoot) -- e.g. extra recoil on a special hit or while reloading. The
 * recoil parameters are configured inline ({@code recoil{Mean_Y=5 Variance_Y=1 ...}}), mirroring a
 * weapon's {@code Shoot.Recoil} block.
 *
 * <p>
 * It reads the firing context from the {@link WeaponCastData} attachment (shooter, weapon, hand)
 * rather than the subject target, so the recoil always lands on whoever fired the weapon. No-op when
 * the shooter is not a player.
 */
public class RecoilMechanic extends Mechanic {

    private RecoilProfile recoil;

    /**
     * Default constructor for serializer.
     */
    public RecoilMechanic() {
    }

    public RecoilMechanic(RecoilProfile recoil) {
        this.recoil = recoil;
    }

    @Override
    public void use0(CastScope scope, Target subject) {
        WeaponCastData data = scope.getAttachment(WeaponCastData.class);
        if (data == null)
            return;
        if (!(data.entityWrapper() instanceof PlayerWrapper playerWrapper))
            return;

        playerWrapper.getRecoilController().onShotFired(recoil, data.weaponTitle(), data.weaponStack(),
            playerWrapper.getPlayer(), data.slot());
    }

    @Override
    public @NotNull TargetKind requiredTarget() {
        // Recoil lands on the shooter from the attachment, never the subject, so it needs no entity.
        return TargetKind.LOCATION;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return new NamespacedKey(WeaponMechanics.getInstance(), "recoil");
    }

    @Override
    protected @NotNull ConfigSchema.Builder schemaBuilder() {
        return super.schemaBuilder()
            .doubleKey("Mean_X").doubleKey("Mean_Y")
            .doubleKey("Variance_X").doubleKey("Variance_Y")
            .doubleKey("Speed").doubleKey("Damping").doubleKey("Damping_Recovery")
            .doubleKey("Smoothing").doubleKey("Max_Accumulation").doubleKey("Recovery_Percentage");
    }

    @Override
    public @NotNull Mechanic serialize(@NotNull SerializeData data) throws SerializerException {
        RecoilProfile profile = new RecoilProfile().serialize(data);
        return applyParentArgs(data, new RecoilMechanic(profile));
    }
}
