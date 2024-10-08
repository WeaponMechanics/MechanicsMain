package me.deecaad.core.utils;

import com.cjcrafter.foliascheduler.ServerImplementation;
import me.deecaad.core.MechanicsCore;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

/**
 * Wraps a bukkit {@link Entity} to use {@link Transform} methods easily. Entity transforms cannot
 * have parents, but they can have children. Not very performance friendly when having many
 * children, since the quaternions are not cached every tick.
 *
 * TODO add cache to deal with potential performance problems
 */
public class EntityTransform extends Transform {

    private final Entity entity;

    public EntityTransform(Entity entity) {
        this.entity = entity;
    }

    @Override
    public Transform getParent() {
        return null; // cannot have a parent
    }

    @Override
    public void setParent(Transform parent) {
        throw new IllegalArgumentException("EntityTransform cannot have parent");
    }

    @Override
    public Vector getLocalPosition() {
        return entity.getLocation().toVector();
    }

    @Override
    public void setLocalPosition(Vector localPosition) {
        ServerImplementation server = MechanicsCore.getPlugin().getFoliaScheduler();
        server.teleportAsync(entity, localPosition.toLocation(entity.getWorld()));
    }

    @Override
    public Quaternion getLocalRotation() {
        Vector view = entity.getLocation().getDirection();
        Vector localUp = Quaternion.UP;
        if (localUp.equals(view))
            localUp = Quaternion.BACKWARD; // TODO improve

        return Quaternion.lookAt(entity.getLocation().getDirection(), localUp);
    }

    @Override
    public void setLocalRotation(Quaternion localRotation) {
        Vector euler = localRotation.getEulerAngles();
        if (entity.getType() == EntityType.ARMOR_STAND) {
            ArmorStand stand = (ArmorStand) entity;
            stand.setHeadPose(new EulerAngle(euler.getX(), euler.getY(), euler.getZ()));
        } else if (MinecraftVersions.UPDATE_AQUATIC.isAtLeast()) {
            entity.setRotation((float) euler.getX(), (float) euler.getY());
        } else {
            Location loc = entity.getLocation();
            loc.setYaw((float) euler.getX());
            loc.setPitch((float) euler.getY());
            entity.teleport(loc); // This call to legacy teleport is fine, since it is 1.12
        }
    }

    @Override
    public void applyRotation(Quaternion rotation) {
        Quaternion local = getLocalRotation();
        local.multiply(rotation);
        setLocalRotation(local);
    }
}
