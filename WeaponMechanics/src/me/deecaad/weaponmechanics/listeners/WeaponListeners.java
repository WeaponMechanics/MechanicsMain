package me.deecaad.weaponmechanics.listeners;

import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;

import static me.deecaad.weaponmechanics.WeaponMechanics.getPlayerWrapper;

public class WeaponListeners implements Listener {

    private WeaponHandler weaponHandler;

    public WeaponListeners(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    // Removed test use temp
    //@EventHandler
    public void interact(PlayerInteractEvent e) {
        Action action = e.getAction();
        boolean rightClick = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
        if (!rightClick) return;

        Player player = e.getPlayer();
        IPlayerWrapper playerWrapper = getPlayerWrapper(player);
        weaponHandler.tryUses(playerWrapper, "test", player.getEquipment().getItemInMainHand(), EquipmentSlot.HAND, TriggerType.RIGHT_CLICK, false);
    }

    @EventHandler (ignoreCancelled = true)
    public void itemHeld(PlayerItemHeldEvent e) {
        IEntityWrapper entityWrapper = WeaponMechanics.getEntityWrapper(e.getPlayer());
        entityWrapper.getMainHandData().cancelTasks();
        entityWrapper.getOffHandData().cancelTasks();
    }

    @EventHandler
    public void death(EntityDeathEvent e) {
        IEntityWrapper entityWrapper = WeaponMechanics.getEntityWrapper(e.getEntity(), true);
        if (entityWrapper == null) return;

        entityWrapper.getMainHandData().cancelTasks();
        entityWrapper.getOffHandData().cancelTasks();
    }

    @EventHandler (ignoreCancelled = true)
    public void click(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        // Off hand is also considered as quickbar slot
        if (e.getSlotType() != InventoryType.SlotType.QUICKBAR) return;

        IEntityWrapper entityWrapper = WeaponMechanics.getEntityWrapper(e.getWhoClicked());
        entityWrapper.getMainHandData().cancelTasks();
        entityWrapper.getOffHandData().cancelTasks();
    }

    @EventHandler (ignoreCancelled = true)
    public void swapHandItems(PlayerSwapHandItemsEvent e) {
        IEntityWrapper entityWrapper = WeaponMechanics.getEntityWrapper(e.getPlayer());
        entityWrapper.getMainHandData().cancelTasks();
        entityWrapper.getOffHandData().cancelTasks();
    }
}