package me.deecaad.weaponmechanics.listeners.trigger;

import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.events.EntityToggleInMidairEvent;
import me.deecaad.weaponmechanics.events.EntityToggleStandEvent;
import me.deecaad.weaponmechanics.events.EntityToggleSwimEvent;
import me.deecaad.weaponmechanics.events.EntityToggleWalkEvent;
import me.deecaad.weaponmechanics.utils.MetadataKey;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class TriggerEntityListeners implements Listener {

    private WeaponHandler weaponHandler;

    public TriggerEntityListeners(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void damageMonitor(EntityDamageByEntityEvent e) {
        Entity victim = e.getEntity();

        // Only when victim has been damaged by WM projectile
        // when Use_Vanilla_Damaging is true
        if (!MetadataKey.VANILLA_DAMAGE.has(victim))
            return;
        MetadataKey.VANILLA_DAMAGE.remove(victim);

        if (e.isCancelled()) {
            // If cancelled set this new meta to let WM know not to use mechanics anymore
            MetadataKey.CANCELLED_DAMAGE.set(victim, null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void damage(EntityDamageByEntityEvent e) {
        Entity victim = e.getEntity();

        if (MetadataKey.VANILLA_DAMAGE.has(victim)) {
            // Don't try melee nor cancel the damage if this entity was just hit by
            // WM projectile while using ´Use_Vanilla_Damaging´.
            return;
        }

        // WeaponMechanics fires an EntityDamageByEntityEvent
        if (victim.hasMetadata("doing-weapon-damage"))
            return;

        // MythicMobs damage skill support. Holding a gun while dealing damage normally triggers this
        if (victim.hasMetadata("skill-damage"))
            return;

        EntityDamageEvent.DamageCause cause = e.getCause();
        boolean isSweep = cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK;
        if (cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK && !isSweep)
            return;
        if (WeaponMechanics.getInstance().getConfiguration().getBoolean("Disabled_Trigger_Checks.Right_And_Left_Click"))
            return;

        Entity damager = e.getDamager();
        if (!damager.getType().isAlive() || !victim.getType().isAlive())
            return;

        if (!(damager instanceof LivingEntity livingEntity) || !(victim instanceof LivingEntity livingVictim))
            return;

        EntityWrapper entityWrapper = WeaponMechanics.getInstance().getEntityWrapper(livingEntity, true);
        if (entityWrapper == null)
            return;

        EntityEquipment entityEquipment = livingEntity.getEquipment();
        if (entityEquipment == null)
            return;

        ItemStack mainStack = entityEquipment.getItemInMainHand();
        String mainWeapon = weaponHandler.getInfoHandler().getWeaponTitle(mainStack, false);

        ItemStack offStack = entityEquipment.getItemInOffHand();
        String offWeapon = weaponHandler.getInfoHandler().getWeaponTitle(offStack, false);

        if (mainWeapon == null && offWeapon == null)
            return;

        boolean dualWield = mainWeapon != null && offWeapon != null;
        boolean mainHasMelee = hasMelee(mainWeapon);

        if (mainWeapon != null) {
            // Cancel vanilla melee and also zero the damage in case another plugin uncancels later
            e.setCancelled(true);
            e.setDamage(0.0);
        }

        // When sweep hit we don't want to do actual melee casts
        if (isSweep)
            return;

        if (weaponHandler.getInfoHandler().denyDualWielding(TriggerType.MELEE, livingEntity.getType() == EntityType.PLAYER ? (Player) livingEntity : null, mainWeapon, offWeapon)) {
            return;
        }

        if (mainWeapon == null || mainStack.getAmount() == 0)
            return;

        if (mainHasMelee) {
            final String weaponTitle = mainWeapon;
            final boolean wasDualWielding = dualWield;

            // Don't apply melee damage inside the vanilla Player.attack(...) stack
            // just let vanilla finish processing the cancelled hit first
            WeaponMechanics.getInstance().getFoliaScheduler().entity(livingVictim).runDelayed(() -> {
                if (!livingEntity.isValid() || livingEntity.isDead())
                    return;

                if (!livingVictim.isValid() || livingVictim.isDead())
                    return;

                EntityEquipment currentEquipment = livingEntity.getEquipment();
                if (currentEquipment == null)
                    return;

                ItemStack currentMainStack = currentEquipment.getItemInMainHand();
                String currentMainWeapon = weaponHandler.getInfoHandler().getWeaponTitle(currentMainStack, false);

                if (!weaponTitle.equals(currentMainWeapon))
                    return;

                EntityWrapper currentWrapper = WeaponMechanics.getInstance().getEntityWrapper(livingEntity, true);
                if (currentWrapper == null)
                    return;

                weaponHandler.tryUses(currentWrapper, weaponTitle, currentMainStack, EquipmentSlot.HAND, TriggerType.MELEE, wasDualWielding, livingVictim);
            }, 1);

            return;
        }
        weaponHandler.tryUses(entityWrapper, mainWeapon, mainStack, EquipmentSlot.HAND, TriggerType.LEFT_CLICK, dualWield, livingVictim);
    }

    private boolean hasMelee(String weaponTitle) {
        if (weaponTitle == null)
            return false;

        return WeaponMechanics.getInstance().getWeaponConfigurations().getBoolean(weaponTitle + ".Melee.Enable_Melee")
                || WeaponMechanics.getInstance().getWeaponConfigurations().getString(weaponTitle + ".Melee.Melee_Attachment") != null;
    }

    @EventHandler
    public void death(EntityDeathEvent e) {
        Entity entity = e.getEntity();

        // Don't remove when its player
        if (entity.getType() == EntityType.PLAYER)
            return;

        // If entity had EntityWrapper data, remove it
        WeaponMechanics.getInstance().removeEntityWrapper(e.getEntity());
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

    @EventHandler(ignoreCancelled = true)
    public void toggleGlide(EntityToggleGlideEvent e) {
        if (WeaponMechanics.getInstance().getConfiguration().getBoolean("Disabled_Trigger_Checks.Glide"))
            return;

        if (!e.getEntityType().isAlive())
            return;
        weaponHandler.useTrigger((LivingEntity) e.getEntity(), e.isGliding() ? TriggerType.START_GLIDE : TriggerType.END_GLIDE, false);
    }
}