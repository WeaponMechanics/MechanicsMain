package me.deecaad.weaponmechanics.weapon.damage;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class ShieldBlocking implements Serializer<ShieldBlocking> {

    public static final ShieldBlocking DEFAULT = new ShieldBlocking(true, false, 120.0, 0, true);

    private boolean stopProjectileWhenBlocked;
    private boolean frontOnly;
    private double blockAngle;

    private int durabilityDamage;
    private boolean onlyWhenProjectileStopped;

    public ShieldBlocking() {
    }

    public ShieldBlocking(boolean stopProjectileWhenBlocked, boolean frontOnly, double blockAngle,
                          int durabilityDamage, boolean onlyWhenProjectileStopped) {
        this.stopProjectileWhenBlocked = stopProjectileWhenBlocked;
        this.frontOnly = frontOnly;
        this.blockAngle = blockAngle;
        this.durabilityDamage = durabilityDamage;
        this.onlyWhenProjectileStopped = onlyWhenProjectileStopped;
    }

    public boolean shouldStopProjectileWhenBlocked() {
        return stopProjectileWhenBlocked;
    }

    public void damageShield(@NotNull Player player, boolean projectileStopped) {
        if (durabilityDamage <= 0)
            return;

        if (onlyWhenProjectileStopped && !projectileStopped)
            return;

        DamageUtil.damageShield(player, durabilityDamage);
    }

    public boolean isBlocking(@NotNull WeaponDamageSource source, @NotNull LivingEntity victim) {
        if (source instanceof MeleeDamageSource meleeSource && meleeSource.isBackStab())
            return false;

        if (!(victim instanceof Player player))
            return false;

        if (!player.isBlocking())
            return false;

        EquipmentSlot shieldSlot = DamageUtil.getShieldSlot(player);
        if (shieldSlot == null)
            return false;

        if (frontOnly && !isWithinBlockAngle(player, source.getDamageLocation()))
            return false;

        return true;
    }

    private boolean isWithinBlockAngle(@NotNull Player player, Location sourceLocation) {
        if (sourceLocation == null)
            return !frontOnly;

        Vector facing = player.getEyeLocation().getDirection();
        facing.setY(0.0);

        if (facing.lengthSquared() == 0.0)
            return true;

        facing.normalize();

        Vector toSource = sourceLocation.toVector().subtract(player.getEyeLocation().toVector());
        toSource.setY(0.0);

        if (toSource.lengthSquared() == 0.0)
            return true;

        toSource.normalize();

        double threshold = Math.cos(Math.toRadians(blockAngle / 2.0));
        return facing.dot(toSource) >= threshold;
    }

    @Override
    public String getKeyword() {
        return "Shield_Blocking";
    }

    @NotNull
    @Override
    public ShieldBlocking serialize(@NotNull SerializeData data) throws SerializerException {
        boolean stopProjectileWhenBlocked = data.of("Stop_Projectile_When_Blocked").getBool().orElse(true);
        boolean frontOnly = data.of("Front_Only").getBool().orElse(false);
        double blockAngle = data.of("Block_Angle").assertRange(0.0, 360.0).getDouble().orElse(120.0);
        int durabilityDamage = data.of("Durability.Damage").assertRange(0, null).getInt().orElse(0);
        boolean onlyWhenProjectileStopped = data.of("Durability.Only_When_Projectile_Stopped").getBool().orElse(true);

        return new ShieldBlocking(stopProjectileWhenBlocked, frontOnly, blockAngle, durabilityDamage, onlyWhenProjectileStopped
        );
    }
}