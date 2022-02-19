package me.deecaad.weaponmechanics.listeners;

import me.deecaad.core.events.EntityEquipmentEvent;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.info.WeaponInfoDisplay;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponEquipEvent;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
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
    public void equip(EntityEquipmentEvent e) {
        if (e.isArmor())
            return;

        LivingEntity entity = (LivingEntity) e.getEntity();
        EntityWrapper entityWrapper = WeaponMechanics.getEntityWrapper(entity);
        ItemStack weaponStack = e.getEquipped();

        // Also try auto converting to weapon
        String weaponTitle = weaponHandler.getInfoHandler().getWeaponTitle(weaponStack, true);
        boolean alreadyUsedEquipMechanics = false;

        if (weaponTitle != null) {
            if (e.getEntityType() == EntityType.PLAYER) {
                WeaponInfoDisplay weaponInfoDisplay = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
                if (weaponInfoDisplay != null) weaponInfoDisplay.send((PlayerWrapper) entityWrapper, weaponTitle, weaponStack);
            }

            weaponHandler.getSkinHandler().tryUse(entityWrapper, weaponTitle, weaponStack, e.getSlot());

            Mechanics equipMechanics = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Equip_Mechanics", Mechanics.class);
            if (equipMechanics != null) {
                equipMechanics.use(new CastData(entityWrapper, weaponTitle, weaponStack));
                alreadyUsedEquipMechanics = true;
            }

            Bukkit.getPluginManager().callEvent(new WeaponEquipEvent(weaponTitle, weaponStack, entity, e.getSlot() == EquipmentSlot.HAND));
        }

        // Don't use holster mechanics is equip mechanics were already used
        if (alreadyUsedEquipMechanics) return;

        if (weaponHandler.getInfoHandler().getWeaponTitle(e.getDequipped(), false) != null) {
            Mechanics holsterMechanics = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Holster_Mechanics", Mechanics.class);
            if (holsterMechanics != null) holsterMechanics.use(new CastData(entityWrapper, weaponTitle, weaponStack));
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

        EntityWrapper entityWrapper = WeaponMechanics.getEntityWrapper(e.getWhoClicked());
        entityWrapper.getMainHandData().cancelTasks();
        entityWrapper.getOffHandData().cancelTasks();
    }

    @EventHandler (ignoreCancelled = true)
    public void swapHandItems(PlayerSwapHandItemsEvent e) {
        EntityWrapper entityWrapper = WeaponMechanics.getEntityWrapper(e.getPlayer());
        entityWrapper.getMainHandData().cancelTasks();
        entityWrapper.getOffHandData().cancelTasks();
    }
}