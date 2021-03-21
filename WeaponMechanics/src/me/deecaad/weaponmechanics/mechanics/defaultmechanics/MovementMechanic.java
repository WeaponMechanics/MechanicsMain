package me.deecaad.weaponmechanics.mechanics.defaultmechanics;

import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.IMechanic;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.io.File;

public class MovementMechanic implements IMechanic<MovementMechanic> {

    private double movementSpeed;
    private boolean towardsTarget;
    private double verticalModifier;

    /**
     * Empty constructor to be used as serializer
     */
    public MovementMechanic() {
        if (Mechanics.hasMechanic(getKeyword())) return;
        Mechanics.registerMechanic(WeaponMechanics.getPlugin(), this);
    }

    public MovementMechanic(double movementSpeed, boolean towardsTarget, double verticalModifier) {
        this.movementSpeed = movementSpeed;
        this.towardsTarget = towardsTarget;
        this.verticalModifier = verticalModifier;
    }

    @Override
    public void use(CastData castData) {

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

            direction = targetLocation.toVector().subtract(casterLocation.toVector());
        } else {
            direction = livingEntity.getLocation().getDirection();
        }
        if (verticalModifier != -99) {
            direction.setY(direction.getY() * verticalModifier);
        }

        livingEntity.setVelocity(direction.normalize().multiply(movementSpeed));
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
    public MovementMechanic serialize(File file, ConfigurationSection configurationSection, String path) {
        double movementSpeed = configurationSection.getDouble(path + ".Movement_Speed", -99);
        if (movementSpeed == -99) {
            return null;
        }
        // Better for configs
        movementSpeed /= 10.0;

        boolean towardsTarget = configurationSection.getBoolean(path + ".Towards_Target");
        double verticalModifier = configurationSection.getDouble(path + ".Vertical_Modifier", -99);

        return new MovementMechanic(movementSpeed, towardsTarget, verticalModifier);
    }
}