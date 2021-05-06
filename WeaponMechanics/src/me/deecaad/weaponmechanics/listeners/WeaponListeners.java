package me.deecaad.weaponmechanics.listeners;

import me.deecaad.core.events.HandDataUpdateEvent;
import me.deecaad.core.events.HandEquipEvent;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.info.WeaponInfoDisplay;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponEquipEvent;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class WeaponListeners implements Listener {

    private WeaponHandler weaponHandler;

    public WeaponListeners(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    @EventHandler
    public void equip(HandEquipEvent e) {
        if (!e.getEntityType().isAlive()) return;

        LivingEntity entity = (LivingEntity) e.getEntity();
        IEntityWrapper entityWrapper = WeaponMechanics.getEntityWrapper(entity);
        ItemStack weaponStack = e.getItemStack();

        // Also try auto converting to weapon
        String weaponTitle = weaponHandler.getInfoHandler().getWeaponTitle(weaponStack, true);

        if (weaponTitle != null) {
            if (e.getEntityType() == EntityType.PLAYER) {
                WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
                if (weaponInfoDisplay != null) weaponInfoDisplay.send((IPlayerWrapper) entityWrapper, weaponTitle, weaponStack);
            }
            Bukkit.getPluginManager().callEvent(new WeaponEquipEvent(weaponTitle, weaponStack, entity, e.isMainHand()));

            weaponHandler.getSkinHandler().tryUse(entityWrapper, weaponTitle, weaponStack, e.isMainHand() ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND);
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void dataUpdate(HandDataUpdateEvent e) {
        // This event is ran async
        if (weaponHandler.getInfoHandler().getWeaponTitle(e.getItemStack(), false) != null) {
            // Simply cancel all weapon NBT changes from being sent to player
            // Expect the visual ones (durability, custom model data, type, name, lore, enchantments)
            e.setCancelled(true);
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void itemHeld(PlayerItemHeldEvent e) {
        WeaponMechanics.getEntityWrapper(e.getPlayer()).getMainHandData().cancelTasks();
        // No need to cancel off hand tasks since this is only called when changing held slot
    }

    @EventHandler
    public void death(EntityDeathEvent e) {
        WeaponMechanics.removeEntityWrapper(e.getEntity());
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