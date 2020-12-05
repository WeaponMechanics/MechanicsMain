package me.deecaad.weaponmechanics.weapon.damage;

import com.google.common.util.concurrent.AtomicDouble;
import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.utils.MaterialHelper;
import me.deecaad.core.utils.NumberUtils;
import me.deecaad.weaponcompatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
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

public class DamageUtils {

    private static Configuration config = WeaponMechanics.getBasicConfigurations();

    /**
     * Do not let anyone instantiate this class
     */
    private DamageUtils() {
    }

    public static double calculateFinalDamage(LivingEntity cause, LivingEntity victim, double damage, DamagePoint point, boolean isBackStab) {
        AtomicDouble rate = new AtomicDouble(1.0);
        IEntityWrapper wrapper = WeaponMechanics.getEntityWrapper(victim);

        // Apply backstab damage
        if (isBackStab) rate.addAndGet(config.getDouble("Damage.Back"));

        // Apply damage per potion effect
        victim.getActivePotionEffects().forEach(potion ->
                rate.addAndGet(config.getDouble("Damage.Potions." + potion.getType().getName())));

        // Apply damage per armor and attachment
        for (ItemStack armorSlot : victim.getEquipment().getArmorContents()) {

            if (armorSlot == null) continue;

            // Note that the material for armor is going to be something like
            // Material.DIAMOND_CHESTPLATE, Material.IRON_BOOTS, Material.GOLDEN_LEGGINGS, Material.LEATHER_HELMET;
            // So split[0] is going to be the material of the armor and
            // split[1] is going to be the armor's slot/type
            String[] split = armorSlot.getType().name().split("_");
            if (split.length == 2) {
                rate.addAndGet(config.getDouble("Damage.Armor." + split[1] + "." + split[0]));
            }

            armorSlot.getEnchantments().forEach((enchant, level) ->
                    rate.addAndGet(config.getDouble("Damage.Armor.Enchantments." + enchant.getKey().getKey())));
        }

        // Apply damage based on victim movement
        if (wrapper.isInMidair()) rate.addAndGet(config.getDouble("Damage.Movement.In_Midair"));
        if (wrapper.isWalking()) rate.addAndGet(config.getDouble("Damage.Movement.Walking"));
        //if (wrapper.isSwimming())  rate.addAndGet(config.getDouble("Damage.Movement.Swimming"));
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
     * @param cause  The cause of the entity's damage
     * @param victim The entity being damaged
     */
    public static void apply(LivingEntity cause, LivingEntity victim, double damage) {

        EntityDamageByEntityEvent entityDamageByEntityEvent = new EntityDamageByEntityEvent(cause, victim, EntityDamageEvent.DamageCause.PROJECTILE, damage);
        Bukkit.getPluginManager().callEvent(entityDamageByEntityEvent);
        if (entityDamageByEntityEvent.isCancelled()) {
            return;
        }

        double newHealth = victim.getHealth() - damage;

        WeaponCompatibilityAPI.getShootCompatibility().logDamage(victim, cause, victim.getHealth(), damage, false);
        victim.setHealth(NumberUtils.minMax(0, newHealth, victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
        victim.playEffect(EntityEffect.HURT);

        victim.setLastDamage(damage);
        victim.setLastDamageCause(entityDamageByEntityEvent);
    }

    public static void damageArmor(LivingEntity victim, int amount) {
        damageArmor(victim, amount, null);
    }

    public static void damageArmor(LivingEntity victim, int amount, @Nullable DamagePoint point) {
        // Stores which armors should be damaged
        ItemStack[] armor;
        EntityEquipment equipment = victim.getEquipment();
        if (point == null) {
            armor = new ItemStack[]{equipment.getHelmet(), equipment.getChestplate(), equipment.getLeggings(), equipment.getBoots()};
        } else {
            switch (point) {
                case HEAD:
                    armor = new ItemStack[]{equipment.getHelmet()};
                    break;
                case BODY:
                case ARMS:
                    armor = new ItemStack[]{equipment.getChestplate()};
                    break;
                case LEGS:
                    armor = new ItemStack[]{equipment.getLeggings()};
                    break;
                case FEET:
                    armor = new ItemStack[]{equipment.getBoots()};
                    break;
                default:
                    throw new IllegalArgumentException("Unknown point: " + point);
            }
        }

        for (ItemStack armorSlot : armor) {
            if (armorSlot == null || MaterialHelper.isAir(armorSlot.getType())) {
                continue;
            }

            if (CompatibilityAPI.getVersion() >= 1.132) {
                if (armorSlot instanceof Damageable) {
                    Damageable meta = (Damageable) armorSlot;
                    meta.setDamage(meta.getDamage() - amount);
                }
            } else {
                armorSlot.setDurability((short) (armorSlot.getDurability() - amount));
            }
        }
    }

    /**
     * @param cause  the cause entity (shooter of projectile)
     * @param victim the victim
     * @return true only if cause can harm victim
     */
    public static boolean canHarm(LivingEntity cause, LivingEntity victim, boolean allowFriendlyFire) {

        boolean allowDamaging = true;

        if (victim.isInvulnerable() || victim.isDead()) {
            allowDamaging = false;
        } else if (victim.getType() == EntityType.PLAYER) {
            Player player = (Player) victim;
            GameMode mode = player.getGameMode();

            allowDamaging = !(mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR);

            // Only do this team check if we can still damage the victim
            if (!allowFriendlyFire && allowDamaging && cause.getType() == EntityType.PLAYER) {
                Scoreboard victimBoard = player.getScoreboard();
                Scoreboard causeBoard = ((Player) cause).getScoreboard();

                Team team1 = victimBoard.getTeam(player.getName());
                Team team2 = causeBoard.getTeam(cause.getName());

                allowDamaging = !team1.equals(team2);
            }
        }

        // False only if teams did not allow damaging
        return allowDamaging;
    }
}
