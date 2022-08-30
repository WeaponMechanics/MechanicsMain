package me.deecaad.weaponmechanics.mechanics.defaultmechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.IMechanic;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.weapon.trigger.Circumstance;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;

public class MovementMechanic implements IMechanic<MovementMechanic> {

    private double movementSpeed;
    private boolean towardsTarget;
    private double verticalSpeed;
    private Circumstance circumstance;

    /**
     * Empty constructor to be used as serializer
     */
    public MovementMechanic() {
        if (Mechanics.hasMechanic(getKeyword())) return;
        Mechanics.registerMechanic(WeaponMechanics.getPlugin(), this);
    }

    public MovementMechanic(double movementSpeed, boolean towardsTarget, double verticalSpeed, Circumstance circumstance) {
        this.movementSpeed = movementSpeed;
        this.towardsTarget = towardsTarget;
        this.verticalSpeed = verticalSpeed;
        this.circumstance = circumstance;
    }

    @Override
    public void use(CastData castData) {

        if (circumstance != null && circumstance.deny(castData.getCasterWrapper())) return;

        LivingEntity livingEntity = castData.getCaster();
        Vector direction;

        if (towardsTarget) {
            Location targetLocation = castData.getData(CommonDataTags.TARGET_LOCATION.name(), Location.class);
            if (targetLocation == null) {
                // There wasn't any target
                return;
            }
            Location casterLocation = livingEntity.getLocation();
            if (!targetLocation.getWorld().getName().equals(casterLocation.getWorld().getName())) {
                return;
            }

            direction = targetLocation.toVector().subtract(casterLocation.toVector()).normalize();
        } else {
            direction = livingEntity.getLocation().getDirection();
        }

        // These are normalized at this point
        if (verticalSpeed != -25) {
            livingEntity.setVelocity(livingEntity.getVelocity().add(direction.multiply(movementSpeed).setY(verticalSpeed)));
        } else {
            livingEntity.setVelocity(livingEntity.getVelocity().add(direction.multiply(movementSpeed)));
        }
    }

    @Override
    public boolean requireEntity() {
        return true;
    }

    @Override
    public String getKeyword() {
        return "Movement";
    }

    @Override
    public boolean shouldSerialize(SerializeData data) {
        // Let Mechanics handle auto serializer stuff
        return false;
    }

    @Override
    @Nonnull
    public MovementMechanic serialize(SerializeData data) throws SerializerException {
        double movementSpeed = data.of("Movement_Speed").assertExists().getDouble();
        boolean towardsTarget = data.of("Towards_Target").getBool(false);
        double verticalSpeed = data.of("Vertical_Speed").getDouble(-500);

        // Divide by 20 to convert from m/s to m/tick
        movementSpeed /= 20.0;
        verticalSpeed /= 20.0;

        Circumstance circumstance = data.of("Circumstance").serialize(Circumstance.class);

        return new MovementMechanic(movementSpeed, towardsTarget, verticalSpeed, circumstance);
    }
}