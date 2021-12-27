package me.deecaad.core.compatibility.entity;

import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

import static me.deecaad.core.compatibility.entity.MetaBitState.*;

/**
 * Defines a packet based {@link org.bukkit.entity.Entity} with no server
 * functions. Faked entities are not ticked, rendered, moved, or in any other
 * way "handled" by the server.
 *
 * <p>Fake entities are usually used for visual effects, since faked entities
 * can control appearances per player.
 *
 * <p>When using a faked entity, you must follow a strict order. First,
 * construct the entity, and set instance variables. Then, show the entity
 * using 1 of the <code>show()</code> method overloads. If you want to show
 * the entity to new players, or at a new time, you must first
 * {@link #clone()} this object before re-sending it. Note that you should
 * probably clone <i>before</i> you use a <code>show()</code> method.
 */
public abstract class FakeEntity implements Cloneable {

    public static final Vector VANILLA_DRAG = new Vector(0.91, 0.98, 0.91);

    private final EntityType type;
    private final Object data; // BlockState/ItemStack

    private final MetaBitState[] meta;
    private Vector velocity;
    private double gravity = 0.08;
    private Vector drag;

    private Consumer<FakeEntity> tickTask;
    protected Object cache;

    public FakeEntity(@Nonnull EntityType type) {
        this(type, null);
    }

    public FakeEntity(@Nonnull ItemStack item) {
        this(EntityType.DROPPED_ITEM, item);
    }

    public FakeEntity(@Nonnull BlockState state) {
        this(EntityType.FALLING_BLOCK, state);
    }

    public FakeEntity(@Nonnull EntityType type, @Nullable Object data) {
        this.type = type;
        this.data = data;
        this.meta = new MetaBitState[]{ RETAIN, RETAIN, RETAIN, RETAIN, RETAIN, RETAIN, RETAIN, RETAIN };
        this.drag = VANILLA_DRAG;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector velocity) {
        this.velocity = velocity;
    }

    public double getGravity() {
        return gravity;
    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    public Vector getDrag() {
        return drag;
    }

    public void setDrag(Vector drag) {
        this.drag = drag;
    }

    public MetaBitState getMetaState(EntityMeta flag) {
        return meta[flag.getIndex()];
    }

    public void setMetaState(EntityMeta flag, MetaBitState state) {
        meta[flag.getIndex()] = state;
    }

    // * ------------------------- * //
    // *       Tick Methods        * //
    // * ------------------------- * //

    public void setTickTask(Consumer<FakeEntity> tickTask) {
        this.tickTask = tickTask;
    }

    public void move() {
        velocity.setY(velocity.getY() - gravity);
        velocity.multiply(drag);
    }

    public void move(@Nonnull Vector motion) {
        move(motion.getX(), motion.getY(), motion.getZ());
    }

    public abstract void move(double x, double y, double z);

    public void look(float yaw, float pitch) {
        look(yaw, pitch, false);
    }

    public abstract void look(float yaw, float pitch, boolean absolute);


    // * ------------------------- * //
    // *   Entity Display Methods  * //
    // * ------------------------- * //

    public void show(@Nonnull Player player, @Nonnull Plugin plugin, @Nonnegative int removeTime, boolean async) {
        if (cache != null) {
            throw new IllegalStateException("Already called show()");
        }

        show0(player);

        // When we do not have a tick task, we only need to remove the entity
        // after the time has elapsed.
        if (tickTask == null) {
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    hide0(player);
                }
            };

            if (async)
                runnable.runTaskLaterAsynchronously(plugin, removeTime);
            else
                runnable.runTaskLater(plugin, removeTime);
        }

        // When we have a tick task, we need to run it once per server tick.
        else {
            BukkitRunnable runnable = new BukkitRunnable() {

                int iterations = 0;

                @Override
                public void run() {
                    if (iterations++ >= removeTime) {
                        hide0(player);
                        cancel();
                        return;
                    }

                    tickTask.accept(FakeEntity.this);
                }
            };

            if (async)
                runnable.runTaskTimerAsynchronously(plugin, 0, 0);
            else
                runnable.runTaskTimer(plugin, 0, 0);
        }
    }

    public void show(@Nonnull Iterable<Player> iterable, @Nonnull Plugin plugin, @Nonnegative int removeTime, boolean async) {
        if (cache != null) {
            throw new IllegalStateException("Already called show()");
        }

        iterable.forEach(this::show0);

        // When we do not have a tick task, we only need to remove the entity
        // after the time has elapsed.
        if (tickTask == null) {
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    iterable.forEach(FakeEntity.this::hide0);
                }
            };

            if (async)
                runnable.runTaskLaterAsynchronously(plugin, removeTime);
            else
                runnable.runTaskLater(plugin, removeTime);
        }

        // When we have a tick task, we need to run it once per server tick.
        else {
            BukkitRunnable runnable = new BukkitRunnable() {

                int iterations = 0;

                @Override
                public void run() {
                    if (iterations++ >= removeTime) {
                        iterable.forEach(FakeEntity.this::hide0);
                        cancel();
                        return;
                    }

                    tickTask.accept(FakeEntity.this);
                }
            };

            if (async)
                runnable.runTaskTimerAsynchronously(plugin, 0, 0);
            else
                runnable.runTaskTimer(plugin, 0, 0);
        }
    }

    protected abstract void show0(@Nonnull Player player);

    protected abstract void hide0(@Nonnull Player player);


    // * ------------------------- * //
    // *        MISC Methods       * //
    // * ------------------------- * //

    @Override
    public FakeEntity clone() {
        return null;
    }
}
