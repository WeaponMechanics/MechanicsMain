package me.deecaad.weaponmechanics.listeners.trigger;

import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.events.EntityToggleInMidairEvent;
import me.deecaad.weaponmechanics.events.EntityToggleStandEvent;
import me.deecaad.weaponmechanics.events.EntityToggleSwimEvent;
import me.deecaad.weaponmechanics.events.EntityToggleWalkEvent;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import static me.deecaad.weaponmechanics.WeaponMechanics.getBasicConfigurations;
import static me.deecaad.weaponmechanics.WeaponMechanics.getEntityWrapper;

public class TriggerEntityListeners implements Listener {

    private WeaponHandler weaponHandler;

    public TriggerEntityListeners(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    @EventHandler
    public void damage(EntityDamageByEntityEvent e) {
        EntityDamageEvent.DamageCause cause = e.getCause();
        if (cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK && cause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) return;
        if (getBasicConfigurations().getBool("Disabled_Trigger_Checks.Right_And_Left_Click")) return;

        Entity damager = e.getDamager();
        Entity victim = e.getEntity();
        if (!damager.getType().isAlive() || !victim.getType().isAlive()) return;

        EntityWrapper entityWrapper = getEntityWrapper((LivingEntity) damager, true);
        if (entityWrapper == null) return;

        LivingEntity livingEntity = entityWrapper.getEntity();

        EntityEquipment entityEquipment = livingEntity.getEquipment();
        if (entityEquipment == null) return;

        ItemStack mainStack = entityEquipment.getItemInMainHand();
        String mainWeapon = weaponHandler.getInfoHandler().getWeaponTitle(mainStack, false);

        ItemStack offStack = entityEquipment.getItemInOffHand();
        String offWeapon = weaponHandler.getInfoHandler().getWeaponTitle(offStack, false);

        if (mainWeapon == null && offWeapon == null) return;

        if (mainWeapon != null) {
            // Cancel melee with weapons by default
            e.setCancelled(true);
        }

        // When sweep hit we don't want to do actual melee casts
        if (cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) return;

        if (weaponHandler.getInfoHandler().denyDualWielding(TriggerType.MELEE,
                livingEntity.getType() == EntityType.PLAYER ? (Player) livingEntity : null, mainWeapon, offWeapon)) return;

        if (mainStack.getAmount() != 0) {
            weaponHandler.tryUses(entityWrapper, mainWeapon, mainStack, EquipmentSlot.HAND, TriggerType.MELEE, mainWeapon != null && offWeapon != null, (LivingEntity) victim);
        }
    }

    @EventHandler
    public void death(EntityDeathEvent e) {
        // If entity had EntityWrapper data, remove it
        WeaponMechanics.removeEntityWrapper(e.getEntity());
    }

    @EventHandler
    public void toggleWalk(EntityToggleWalkEvent e) {
        // Whether this is used its checked already in MoveTask class
        weaponHandler.useTrigger(e.getLivingEntity(), e.isWalking() ? TriggerType.START_WALK : TriggerType.END_WALK, false);
    }

    @EventHandler
    public void toggleInMidair(EntityToggleInMidairEvent e) {
        // Whether this is used its checked already in MoveTask class
        weaponHandler.useTrigger(e.getLivingEntity(), e.isInMidair() ? TriggerType.START_IN_MIDAIR : TriggerType.END_IN_MIDAIR, false);
    }

    @EventHandler
    public void toggleStand(EntityToggleStandEvent e) {
        // Whether this is used its checked already in MoveTask class
        weaponHandler.useTrigger(e.getLivingEntity(), e.isStanding() ? TriggerType.START_STAND : TriggerType.END_STAND, false);
    }

    @EventHandler
    public void toggleSwim(EntityToggleSwimEvent e) {
        // Whether this is used its checked already in MoveTask class
        weaponHandler.useTrigger(e.getLivingEntity(), e.isSwimming() ? TriggerType.START_SWIM : TriggerType.END_SWIM, false);
    }

    @EventHandler (ignoreCancelled = true)
    public void toggleGlide(EntityToggleGlideEvent e) {
        if (getBasicConfigurations().getBool("Disabled_Trigger_Checks.Glide")) return;

        if (!e.getEntityType().isAlive()) return;
        weaponHandler.useTrigger((LivingEntity) e.getEntity(), e.isGliding() ? TriggerType.START_GLIDE : TriggerType.END_GLIDE, false);
    }
}