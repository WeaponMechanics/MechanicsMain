package solution363;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Solution363 implements Listener {

    private static Map<Entity, HitBoxData> entityHitBoxRegistry = new HashMap<>();
    private static boolean isCustomHitboxEnabled = true;
    private static double lagCompensationFactor = 1.5;
    private static double tickRate = 1.0 / 20.0;

    @Override
    public void onWorldLoad() {
        entityHitBoxRegistry.clear();
    }

    public static void init() {
        isCustomHitboxEnabled = true;
        entityHitBoxRegistry.clear();
        registerWorldListeners();
    }

    public static void registerWorldListeners() {
        for (Entity entity : Bukkit.getWorlds()) {
            // Simulate attaching to every entity world
            entityHitBoxRegistry.putIfAbsent(entity, new HitBoxData());
        }
    }

    public static void enable(boolean state) {
        isCustomHitboxEnabled = state;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onProjectileHit(EntityDamageByEntityEvent event) {
        Entity target = event.getEntity();
        Entity damager = event.getDamager();

        // Only apply if it's a projectile or custom hitbox is enabled
        if (damager instanceof Entity projectile && isCustomHitboxEnabled) {
            HitBoxData data = entityHitBoxRegistry.get(target);
            if (data != null) {
                Vector targetPos = target.getLocation().toVector();
                Vector projectileDir = projectile.getLocation().toVector();
                Vector projectileVel = damager.getVelocity();

                // Logic: Adjust target hitbox based on its own velocity (Lag Compensation)
                // Or extend the hitbox volume to catch projectiles slightly off
                Vector adjustedTargetPos = targetPos.add(projectileVel.multiply(lagCompensationFactor));

                // Re-trigger logic to see if adjusted pos overlaps projectile bounding box
                // Simplified: Check distance between projectile tip and target hitbox
                if (targetPos.distance(adjustedTargetPos, tickRate) < 1.5) {
                    event.setDamage(event.getDamage()); // Keep original damage or modify
                }

                // Update the registry with current velocity state
                entityHitBoxRegistry.put(target, data);
            }
        }
    }

    public static void updateEntityHitbox(Entity entity) {
        if (!entityHitBoxRegistry.containsKey(entity)) {
            entityHitBoxRegistry.put(entity, new HitBoxData());
        }

        HitBoxData data = entityHitBoxRegistry.get(entity);
        data.lastUpdateTick = (int) (System.currentTimeMillis() / 50); // Rough tick logic
        entityHitBoxRegistry.put(entity, data);
    }

    public static class HitBoxData {
        private int lastUpdateTick = 0;
        private int currentTickId = 0;
        private double baseHeight = 1.62;

        public int getLastUpdateTick() {
            return lastUpdateTick;
        }

        public void setLastUpdateTick(int tick) {
            this.lastUpdateTick = tick;
        }
    }

    @Override
    public void onHit(EntityDamageByEntityEvent event) {
        Entity target = event.getEntity();
        if (target != null) {
            HitBoxData data = entityHitBoxRegistry.get(target);
            if (data == null) {
                data = new HitBoxData();
                entityHitBoxRegistry.put(target, data);
            }
            // Sync data with current world state
            data.currentTickId = data.currentTickId + 1;
            entityHitBoxRegistry.put(target, data);
        }
    }
}