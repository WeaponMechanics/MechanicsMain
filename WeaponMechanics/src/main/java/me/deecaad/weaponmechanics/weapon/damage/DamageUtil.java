package me.deecaad.weaponmechanics.weapon.damage;

import com.google.common.util.concurrent.AtomicDouble;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.utils.MetadataKey;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Statistic;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import javax.annotation.Nullable;
import java.util.Set;

import static me.deecaad.weaponmechanics.WeaponMechanics.getBasicConfigurations;

public class DamageUtil {
    
    /**
     * Do not let anyone instantiate this class
     */
    private DamageUtil() { }

    public static double calculateFinalDamage(LivingEntity cause, LivingEntity victim, double damage, DamagePoint point, boolean isBackStab) {
        Configuration config = WeaponMechanics.getBasicConfigurations();

        // Simply don't use rates when using vanilla damaging
        if (config.getBool("Damage.Use_Vanilla_Damaging", false)) {
            return damage;
        }

        AtomicDouble rate = new AtomicDouble(1.0);
        EntityWrapper wrapper = WeaponMechanics.getEntityWrapper(victim);

        // Apply backstab damage
        if (isBackStab) rate.addAndGet(config.getDouble("Damage.Back"));

        // Apply damage per potion effect
        for (PotionEffect potion : victim.getActivePotionEffects()) {
            rate.addAndGet(config.getDouble("Damage.Potions." + potion.getType().getName()));
        }

        // Apply damage per armor and attachment
        for (ItemStack armorSlot : victim.getEquipment().getArmorContents()) {
            if (armorSlot == null)
                continue;

            // We parse Material and EquipmentSlot from the given item. Note
            // that all armor names are formatted like: DIAMOND_CHESTPLATE,
            // IRON_BOOTS, LEATHER_HELMET, hence Material_Equipment slot.
            // Normally we would split at the '_', but using String#split is
            // ~50x slower than String#subString.
            String name = armorSlot.getType().name();
            int splitIndex = name.indexOf('_');
            if (splitIndex == -1)
                continue;

            // This method of parsing material and slot has issues with
            // materials like ACACIA_BOAT. In this case, it will fail silently
            // by adding 0.0 to the rate.
            String material = name.substring(0, splitIndex);
            String slot = name.substring(splitIndex + 1);

            rate.addAndGet(config.getDouble("Damage.Armor." + slot + "." + material, 0.0));

            // Reduce damage based on entity type, #110
            rate.addAndGet(config.getDouble("Damage.Entities." + victim.getType(), 0.0));

            if (ReflectionUtil.getMCVersion() < 13) {
                armorSlot.getEnchantments().forEach((enchant, level) ->
                        rate.addAndGet(level * config.getDouble("Damage.Armor.Enchantments." + enchant.getName())));
            } else {
                armorSlot.getEnchantments().forEach((enchant, level) ->
                        rate.addAndGet(level * config.getDouble("Damage.Armor.Enchantments." + enchant.getKey().getKey())));
            }
        }

        // Apply damage based on victim movement
        if (wrapper.isInMidair()) rate.addAndGet(config.getDouble("Damage.Movement.In_Midair"));
        if (wrapper.isWalking()) rate.addAndGet(config.getDouble("Damage.Movement.Walking"));
        if (wrapper.isSwimming())  rate.addAndGet(config.getDouble("Damage.Movement.Swimming"));
        if (wrapper.isSprinting()) rate.addAndGet(config.getDouble("Damage.Movement.Sprinting"));
        if (wrapper.isSneaking()) rate.addAndGet(config.getDouble("Damage.Movement.Sneaking"));

        // Apply damage based on the point that hit the victim
        if (point != null) {
            rate.addAndGet(config.getDouble("Damage.Critical_Points." + point.name()));
        }

        // Make sure damage is within ranges
        rate.set(Math.min(rate.get(), config.getDouble("Damage.Maximum_Rate")));
        rate.set(Math.max(rate.get(), config.getDouble("Damage.Minimum_Rate")));

        // Apply damage to victim
        return damage * rate.get();
    }

    /**
     * @param cause The cause of the entity's damage
     * @param victim The entity being damaged
     * @return true if damage was cancelled
     */
    public static boolean apply(LivingEntity cause, LivingEntity victim, double damage) {

        if (victim.isInvulnerable() || victim.isDead()) {
            return true;
        } else if (victim.getType() == EntityType.PLAYER) {
            GameMode gamemode = ((Player) victim).getGameMode();

            if (gamemode == GameMode.CREATIVE || gamemode == GameMode.SPECTATOR) {
                return true;
            }
        }

        if (damage < 0) {
            damage = 0;
        }

        if (getBasicConfigurations().getBool("Damage.Use_Vanilla_Damaging", false)) {

            if (damage == 0) {
                return true;
            }

            MetadataKey.VANILLA_DAMAGE.set(victim, null);

            victim.damage(damage, cause);

            if (MetadataKey.CANCELLED_DAMAGE.has(victim)) {
                MetadataKey.CANCELLED_DAMAGE.remove(victim);

                // Damage was cancelled
                return true;
            }

            // Vanilla thing to allow constant hits from projectiles
            victim.setNoDamageTicks(0);

            // Successfully damaged using vanilla damaging
            return false;
        }

        // For compatibility with plugins that only set the damage to 0.0...
        double tempDamage = damage;

        EntityDamageByEntityEvent entityDamageByEntityEvent = new EntityDamageByEntityEvent(cause, victim, EntityDamageEvent.DamageCause.PROJECTILE, damage);
        Bukkit.getPluginManager().callEvent(entityDamageByEntityEvent);
        if (entityDamageByEntityEvent.isCancelled()) {
            return true;
        }

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
