package me.deecaad.core.compatibility.entity;

import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class ProjectileFakeEntity extends FakeEntity {

    private Object projectile; // Would be AProjectile in the new code

    public ProjectileFakeEntity(@NotNull EntityType type) {
        super(type);
        setVelocity(new Vector(0, 0, 0));
        setTickTask(fakeEntity -> {
            boolean dead = false;

            if (dead) { // projectile.isDead()

                hide0(null); // Hide for players who this fake entiy has been shown to

                // Need to be able to cancel task

                return;
            }

            // Update since not dead
            Vector vv = new Vector(0, 0, 0); // projectile.getMotion
            move(vv.getX(), vv.getY(), vv.getZ());
        });
    }

    public ProjectileFakeEntity(@NotNull ItemStack item) {
        super(item);
        // Same as first const
    }

    public ProjectileFakeEntity(@NotNull BlockState state) {
        super(state);
        // Same as first const
    }

    @Override
    public void move(double x, double y, double z) {
        // Calculate yaw and pitch based on motion (x, y, z)

        // Send for all players for whom this entity is shown
        // Send velocity packet
        // Send teleport packet if motion length > 8
        // Send position rotation packet if motion length < 8
    }

    @Override
    public void look(float yaw, float pitch, boolean absolute) {
        // Not needed for projectile fake entity since these need to be updated with same
        // packet used in entity move(double, double, double)
    }

    @Override
    protected void show0(@NotNull Player player) {
        // Spawn packet, metadata packet (dropped item), head rotation packet
        // Call move(double, double, double) once
    }

    @Override
    protected void hide0(@NotNull Player player) {
        // Destroy packet
    }
}
