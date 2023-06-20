package me.deecaad.weaponmechanics.weapon.damage;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.utils.MetadataKey;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Statistic;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import javax.annotation.Nullable;
import java.util.Set;

import static me.deecaad.weaponmechanics.WeaponMechanics.getBasicConfigurations;

public class DamageUtil {
    
    /**
     * Do not let anyone instantiate this class
     */
    private DamageUtil() {
    }

    /**
     * @param cause The shooter that caused the damage.
     * @param victim The victim being damaged.
     * @param damage The amount of damage to apply
     * @return true if damage was cancelled
     */
    public static boolean apply(LivingEntity cause, LivingEntity victim, double damage) {

        if (victim.isInvulnerable() || victim.isDead())
            return true;

        // Make sure the player is not in creative or spectator, can only damage survival/adventure
        if (victim.getType() == EntityType.PLAYER) {
            GameMode gamemode = ((Player) victim).getGameMode();

            if (gamemode == GameMode.CREATIVE || gamemode == GameMode.SPECTATOR)
                return true;
        }

        // Use enderman teleport API added in 1.20.1
        else if (victim.getType() == EntityType.ENDERMAN && ReflectionUtil.getMCVersion() >= 20) {
            Enderman enderman = (Enderman) victim;

            // Teleport randomly if the enderman is calm, otherwise assume their
            // target is the shooter, and teleport towards the shooter.
            if (enderman.getTarget() == null)
                enderman.teleport();
            else
                enderman.teleportTowards(cause);

            return true;
        }

        // Skip damaging if possible
        if (damage < 0)
            damage = 0;

        if (getBasicConfigurations().getBool("Damage.Use_Vanilla_Damaging", false)) {
            if (damage == 0)
                return true;

            // VANILLA_DAMAGE is used to make sure we don't do melee trigger checks
            // on EntityDamageByEntityEvent. null doesn't matter, the metadata key
            // is still applied to the entity.
            MetadataKey.VANILLA_DAMAGE.set(victim, null);

            victim.damage(damage, cause);

            // If the EntityDamageByEntityEvent was cancelled, then we should skip
            // everything else.
            if (MetadataKey.CANCELLED_DAMAGE.has(victim)) {
                MetadataKey.CANCELLED_DAMAGE.remove(victim);
                return true;
            }

            // Vanilla thing to allow constant hits from projectiles
            victim.setNoDamageTicks(0);
            return false;
        }

        // For compatibility with plugins that only set the damage to 0.0...
        double tempDamage = damage;

        EntityDamageByEntityEvent entityDamageByEntityEvent = new EntityDamageByEntityEvent(cause, victim, EntityDamageEvent.DamageCause.PROJECTILE, damage);
        Bukkit.getPluginManager().callEvent(entityDamageByEntityEvent);
        if (entityDamageByEntityEvent.isCancelled())
            return true;

        // Doing getDamage() is enough since only BASE modifier is used in event call above ^^
        damage = entityDamageByEntityEvent.getDamage();
        if (tempDamage != damage && damage == 0.0) {
            // If event changed damage, and it's now 0.0, consider this as cancelled damage event
            return true;
        }

        // Calculate the amount of damage to absorption hearts, and
        // determine how much damage is left over to deal to the victim
        double absorption = CompatibilityAPI.getEntityCompatibility().getAbsorption(victim);
        double absorbed = Math.max(0, absorption - damage);
        CompatibilityAPI.getEntityCompatibility().setAbsorption(victim, absorbed);
        damage = Math.max(damage - absorption, 0);

        double oldHealth = victim.getHealth();

        // Apply any remaining damage to the victim, and handle internals
        WeaponCompatibilityAPI.getWeaponCompatibility().logDamage(victim, cause, oldHealth, damage, false);
        if (cause.getType() == EntityType.PLAYER) {
            WeaponCompatibilityAPI.getWeaponCompatibility().setKiller(victim, (Player) cause);
        }

        // Visual red flash
        WeaponCompatibilityAPI.getWeaponCompatibility().playHurtAnimation(victim);

        // Spigot api things
        victim.setLastDamage(damage);
        victim.setLastDamageCause(entityDamageByEntityEvent);

        victim.setHealth(NumberUtil.minMax(0, oldHealth - damage, victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
        boolean killed = victim.isDead() || victim.getHealth() <= 0.0;

        // Statistics
        if (victim instanceof Player player) {
            if (ReflectionUtil.getMCVersion() >= 13 && absorbed >= 0.1) player.incrementStatistic(Statistic.DAMAGE_ABSORBED, Math.round((float) absorbed * 10));
            if (damage >= 0.1) player.incrementStatistic(Statistic.DAMAGE_TAKEN, Math.round((float) damage * 10));
            if (killed && isWhitelisted(cause.getType())) player.incrementStatistic(Statistic.ENTITY_KILLED_BY, cause.getType());
        }
        if (cause instanceof Player player) {
            if (ReflectionUtil.getMCVersion() >= 13 && absorbed >= 0.1) player.incrementStatistic(Statistic.DAMAGE_DEALT_ABSORBED, Math.round((float) absorbed * 10));
            if (damage >= 0.1) player.incrementStatistic(Statistic.DAMAGE_DEALT, Math.round((float) damage * 10));
            if (killed) {
                if (isWhitelisted(victim.getType())) player.incrementStatistic(Statistic.KILL_ENTITY, victim.getType());

                // In newer versions (probably 1.13, but only confirmed in 1.18.2+),
                // these statistics are automatically tracked.
                if (ReflectionUtil.getMCVersion() < 13) {
                    if (victim.getType() == EntityType.PLAYER) player.incrementStatistic(Statistic.PLAYER_KILLS);
                    else player.incrementStatistic(Statistic.MOB_KILLS);
                }
            }
        }

        return false;
    }

    /**
     * Mobs without spawn eggs didn't have statistics associated with them
     * before 1.13. See https://bugs.mojang.com/browse/MC-33710.
     *
     * @param type The entity type.
     * @return false if there is no statistic.
     */
    public static boolean isWhitelisted(EntityType type) {
        if (ReflectionUtil.getMCVersion() >= 13)
            return true;

        return switch (type) {
            case IRON_GOLEM, SNOWMAN, ENDER_DRAGON, WITHER, GIANT, PLAYER -> false;
            default -> ReflectionUtil.getMCVersion() != 12 || type != EntityType.ILLUSIONER;
        };
    }
    
    public static void damageArmor(LivingEntity victim, int amount) {
        damageArmor(victim, amount, null);
    }
    
    public static void damageArmor(LivingEntity victim, int amount, @Nullable DamagePoint point) {

        // If the damage amount is 0, we can skip the calculations
        if (amount <= 0)
            return;

        // Stores which armors should be damaged
        EntityEquipment equipment = victim.getEquipment();
        if (equipment == null)
            return;

        if (point == null) {
            ItemStack helmet = damage(equipment.getHelmet(), amount);
            equipment.setHelmet(helmet);
            ItemStack chestplate = damage(equipment.getChestplate(), amount);
            equipment.setChestplate(chestplate);
            ItemStack leggings = damage(equipment.getLeggings(), amount);
            equipment.setLeggings(leggings);
            ItemStack boots = damage(equipment.getBoots(), amount);
            equipment.setBoots(boots);
        } else {
            switch (point) {
                case HEAD -> {
                    ItemStack helmet = damage(equipment.getHelmet(), amount);
                    equipment.setHelmet(helmet);
                }
                case BODY, ARMS -> {
                    ItemStack chestplate = damage(equipment.getChestplate(), amount);
                    equipment.setChestplate(chestplate);
                }
                case LEGS -> {
                    ItemStack leggings = damage(equipment.getLeggings(), amount);
                    equipment.setLeggings(leggings);
                }
                case FEET -> {
                    ItemStack boots = damage(equipment.getBoots(), amount);
                    equipment.setBoots(boots);
                }
                default -> throw new IllegalArgumentException("Unknown point: " + point);
            }
        }
    }

    private static ItemStack damage(ItemStack armor, int amount) {
        if (armor == null || "AIR".equals(armor.getType().name()))
            return null;

        if (armor.hasItemMeta()) {
            int level = armor.getItemMeta().getEnchantLevel(Enchantment.DURABILITY);
            boolean damages = NumberUtil.chance(0.6 + 0.4 / (level + 1));

            if (!damages)
                return armor;
        }

        if (ReflectionUtil.getMCVersion() >= 13) {
            if (armor.getItemMeta() instanceof Damageable meta) {
                meta.setDamage(Math.min(meta.getDamage() + amount, armor.getType().getMaxDurability()));
                armor.setItemMeta(meta);
            }
        } else {
            armor.setDurability((short) (armor.getDurability() + amount));
        }

        return armor;
    }
    
    /**
     * @param cause the cause entity (shooter of projectile)
     * @param victim the victim
     * @return true only if cause can harm victim
     */
    public static boolean canHarmScoreboardTeams(LivingEntity cause, LivingEntity victim) {

        // Only check scoreboard teams for players
        if (cause.getType() != EntityType.PLAYER || victim.getType() != EntityType.PLAYER) return true;

        Scoreboard shooterScoreboard = ((Player) cause).getScoreboard();
        if (shooterScoreboard == null) return true;

        Set<Team> teams = shooterScoreboard.getTeams();
        if (teams == null || teams.isEmpty()) return true;

        for (Team team : teams) {
            Set<String> entries = team.getEntries();

            // Seems like this has to be also checked...
            if (!entries.contains(cause.getName())) continue;

            // If not in same team -> continue
            if (!entries.contains(victim.getName())) continue;

            // Now we know they're in same team.
            // -> If friendly is not enabled
            // --> they can't harm each other
            if (!team.allowFriendlyFire()) {
                // This approach only checks first same team WHERE friendly fire is enabled
                return false;
            }
        }

        return true;
    }
}
