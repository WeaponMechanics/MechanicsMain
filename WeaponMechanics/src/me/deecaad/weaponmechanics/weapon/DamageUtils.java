package me.deecaad.weaponmechanics.weapon;

import com.google.common.util.concurrent.AtomicDouble;
import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.utils.DebugUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class DamageUtils {
    
    private static final double UPDATE = Double.MIN_VALUE;
    private static Configuration config = WeaponMechanics.getBasicConfigurations();
    
    /**
     * Do not let others construct
     * this class
     */
    private DamageUtils() {
    }
    
    /**
     * @param cause The cause of the entity's damage
     * @param victim The entity being damaged
     * @param damage The amount of damage to apply to the entity
     * @param point Where the victim was hit
     * @param isBackStab Whether or not the hit was a backstab
     * @return The amount of damage applied
     */
    public static double apply(LivingEntity cause, LivingEntity victim, double damage, @Nonnull DamagePoint point, boolean isBackStab) {
        return apply(cause, victim, damage, point, isBackStab, false);
    }
    
    /**
     * @param cause The cause of the entity's damage
     * @param victim The entity being damaged
     * @param point Where the victim was hit
     * @param isBackStab Whether or not the hit was a backstab
     * @param isSkipCalculations Whether or not to skip calculations
     * @return The amount of damage applied
     */
    public static double apply(LivingEntity cause, LivingEntity victim, double damage, @Nonnull DamagePoint point, boolean isBackStab, boolean isSkipCalculations) {
        if (isSkipCalculations) {
            victim.setHealth(victim.getHealth() - damage);
            victim.damage(UPDATE, cause);
            return damage;
        }
        AtomicDouble rate = new AtomicDouble(1.0);
        IEntityWrapper wrapper = WeaponMechanics.getEntityWrapper(victim);
        
        // Apply backstab damage
        if (isBackStab) rate.addAndGet(config.getDouble("Damage.Back"));
        
        // Apply damage per potion effect
        victim.getActivePotionEffects().forEach(potion ->
                rate.addAndGet(config.getDouble("Damage.Potions." + potion.getType().getName())));
        
        // Apply damage per armor and attachment
        for (ItemStack armorSlot : victim.getEquipment().getArmorContents()) {
 
            // Note that the material for armor is going to be something like
            // Material.DIAMOND_CHESTPLATE, Material.IRON_BOOTS, Material.GOLDEN_LEGGINGS, Material.LEATHER_HELMET;
            // So split[0] is going to be the material of the armor and
            // split[1] is going to be the armor's slot/type
            String[] split = armorSlot.getType().name().split("_");
            rate.addAndGet(config.getDouble("Damage.Armor." + split[1] + "." + split[0]));
            
            armorSlot.getEnchantments().forEach((enchant, level) ->
                    rate.addAndGet(config.getDouble("Damage.Armor.Enchantments." + enchant.getKey().getKey())));
        }
        
        // Apply damage based on victim movement
        if (wrapper.isInMidair())  rate.addAndGet(config.getDouble("Damage.Movement.In_Midair"));
        if (wrapper.isWalking())   rate.addAndGet(config.getDouble("Damage.Movement.Walking"));
        //if (wrapper.isSwimming())  rate.addAndGet(config.getDouble("Damage.Movement.Swimming"));
        if (wrapper.isSprinting()) rate.addAndGet(config.getDouble("Damage.Movement.Sprinting"));
        if (wrapper.isSneaking())  rate.addAndGet(config.getDouble("Damage.Movement.Sneaking"));
        
        // Apply damage based on the point that hit the victim
        rate.addAndGet(config.getDouble("Damage.Critical_Points." + point.name()));
        
        // Make sure damage is within ranges
        rate.set(Math.min(rate.get(), config.getDouble("Damage.Maximum_Rate")));
        rate.set(Math.max(rate.get(), config.getDouble("Damage.Minimum_Rate")));
        
        // Apply damage to victim
        double damageAmount = damage * rate.get();
        
        if (victim.getHealth() - damageAmount <= 0.0) {
            
            // Setting health really low allows us to
            // avoid armor/resistance/blocking/whatever
            // so damaging later should be able to kill
            // the victim. This should kill the victim
            // every time.
            victim.setHealth(0.0001);
            victim.damage(200, cause);
        }
        else {
            // Apply damage to player
            victim.setHealth(victim.getHealth() - damageAmount);
        }
    
        DebugUtil.log(LogLevel.DEBUG, victim + " damaged by " + cause + " for " + damageAmount + " damage.");
        
        return damageAmount;
    }
    
    public static void damageArmor(LivingEntity victim, int amount) {
        damageArmor(victim, amount, null);
    }
    
    public static void damageArmor(LivingEntity victim, int amount, @Nullable DamagePoint point) {
        // Stores which armors should be damaged
        Set<ItemStack> armor = new HashSet<>();
        switch (point) {
            case HEAD:
                armor.add(victim.getEquipment().getHelmet());
                break;
            case BODY: case ARMS:
                armor.add(victim.getEquipment().getChestplate());
                break;
            case LEGS:
                armor.add(victim.getEquipment().getLeggings());
                armor.add(victim.getEquipment().getBoots());
                break;
            default:
                armor.addAll(Arrays.stream(victim.getEquipment().getArmorContents()).collect(Collectors.toSet()));
                break;
        }
    
        for (ItemStack armorSlot : armor) {
            // Although this check isn't necessary (because
            // the method was replaced with the newer damageable
            // metadata), better to have this here in case the method
            // is marked for removal
            if (CompatibilityAPI.getVersion() >= 1.132) {
                Damageable meta = (org.bukkit.inventory.meta.Damageable) armorSlot;
                meta.setDamage(meta.getDamage() - amount);
            } else {
                armorSlot.setDurability((short) (armorSlot.getDurability() - amount));
            }
        }
    }
    
    /**
     * @param cause the cause entity (shooter of projectile)
     * @param victim the victim
     * @return true only if cause can harm victim
     */
    public static boolean canHarm(LivingEntity cause, LivingEntity victim) {
        
        // Call entity damage by entity event to see if other plugins want to cancel this damage
        EntityDamageByEntityEvent entityDamageByEntityEvent = new EntityDamageByEntityEvent(cause, victim, EntityDamageEvent.DamageCause.PROJECTILE, 1.0);
        Bukkit.getPluginManager().callEvent(entityDamageByEntityEvent);
        
        if (entityDamageByEntityEvent.isCancelled()) {
            // Can not harm because cancelled
            return false;
        }
        
        boolean allowDamaging = true;
        
        // Check for MC own scoreboard teams
        if (cause instanceof Player && victim instanceof Player) {
            Scoreboard shooterScoreboard = ((Player) cause).getScoreboard();
            
            if (shooterScoreboard != null) {
                Player victimPlayer = (Player) victim;
                
                // Iterate through shooter's teams
                for (Team team : shooterScoreboard.getTeams()) {
                    if (!team.getEntries().contains(victimPlayer.getName()) || team.allowFriendlyFire()) {
                        // Not in the same team
                        // OR
                        // Team allows damaging teammates
                        continue;
                    }
                    // Else damaging is not allowed -> allowDamaging false
                    allowDamaging = false;
                    break;
                }
            }
        }
        
        // False only if teams did not allow damaging
        return allowDamaging;
    }
}
