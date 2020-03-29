package me.deecaad.weaponmechanics.listeners;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.weaponmechanics.events.PlayerJumpEvent;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import static me.deecaad.weaponmechanics.WeaponMechanics.*;

public class PlayerListeners implements Listener {

    private WeaponHandler weaponHandler;

    public PlayerListeners(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    @EventHandler
    public void respawn(PlayerRespawnEvent e) {
        // Add PlayerWrapper because it is cleared in EntityDeathEvent (PlayerDeathEvent extends EntityDeathEvent)
        getPlayerWrapper(e.getPlayer());
    }

    @EventHandler
    public void join(PlayerJoinEvent e) {
        // Add PlayerWrapper
        getPlayerWrapper(e.getPlayer());
    }

    @EventHandler
    public void quit(PlayerQuitEvent e) {
        // Remove EntityWrapper data and cancel move task
        removeEntityWrapper(e.getPlayer());
    }

    @EventHandler (ignoreCancelled = true)
    public void toggleSneak(PlayerToggleSneakEvent e) {
        if (getBasicConfigurations().getBool("Disabled_Trigger_Checks.Sneak")) return;

        weaponHandler.useTrigger(e.getPlayer(), e.isSneaking() ? TriggerType.START_SNEAK : TriggerType.END_SNEAK, false);
    }

    @EventHandler (ignoreCancelled = true)
    public void toggleSprint(PlayerToggleSprintEvent e) {
        if (getBasicConfigurations().getBool("Disabled_Trigger_Checks.Sprint")) return;

        weaponHandler.useTrigger(e.getPlayer(), e.isSprinting() ? TriggerType.START_SPRINT : TriggerType.END_SPRINT, false);

    }

    @EventHandler
    public void jump(PlayerJumpEvent e) {
        // Whether this is used its checked already in MoveTask class
        weaponHandler.useTrigger(e.getPlayer(), TriggerType.JUMP, false);
    }

    @EventHandler
    public void interact(PlayerInteractEvent e) {
        Action action = e.getAction();

        // I don't think ignoreCancelled = true works in this event properly
        if (action == Action.PHYSICAL || e.useItemInHand() == Event.Result.DENY) return;

        if (getBasicConfigurations().getBool("Disabled_Trigger_Checks.Right_And_Left_Click")) return;

        boolean useOffHand = CompatibilityAPI.getVersion() >= 1.09;

        Player player = e.getPlayer();

        boolean rightClick = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
        if (rightClick) getPlayerWrapper(player).rightClicked();
        boolean isBlockInteraction = action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK;

        ItemStack mainStack;
        if (useOffHand) {
            mainStack = player.getEquipment().getItemInMainHand();

            // getHand() can't be null because Action.PHYSICAL is already denied above
            if (e.getHand() == EquipmentSlot.OFF_HAND && mainStack.getType() != Material.AIR) {
                // Basically this just cancels double calls to player interact event
                // -> This event is called once for both hands so this simply checks if
                // off hand is used and if main hand item wasn't air.
                // --> Meaning that it main stack was something else than AIR this event would have been already called once
                return;
            }
        } else {
            // 1.8
            mainStack = player.getEquipment().getItemInHand();
        }

        Bukkit.broadcastMessage("asd");

        String mainWeapon = weaponHandler.getInfoHandler().getWeaponTitle(mainStack, true);

        // Only get off hand things is server is 1.9 or newer
        ItemStack offStack = null;
        String offWeapon = null;
        if (useOffHand) {
            offStack = player.getEquipment().getItemInOffHand();
            offWeapon = weaponHandler.getInfoHandler().getWeaponTitle(offStack, true);
        }

        if (mainWeapon == null && offWeapon == null) return;

        if (isBlockInteraction && (mainWeapon != null && getConfigurations().getBool(mainWeapon + ".Info.Cancel.Block_Interactions", true)
                || offWeapon != null && getConfigurations().getBool(offWeapon + ".Info.Cancel.Block_Interactions", true))) {
            e.setUseInteractedBlock(Event.Result.DENY);
        }

        if (!isBlockInteraction && (mainWeapon != null && getConfigurations().getBool(mainWeapon + ".Info.Cancel.Item_Interactions")
                || offWeapon != null && getConfigurations().getBool(offWeapon + ".Info.Cancel.Item_Interactions"))) {
            e.setUseItemInHand(Event.Result.DENY);
        }

        IPlayerWrapper playerWrapper = getPlayerWrapper(player);

        if (rightClick) {
            // Only do dual wield check if server is 1.9 or newer
            if (useOffHand && !weaponHandler.getInfoHandler().allowDualWielding(TriggerType.RIGHT_CLICK, player, mainWeapon, offWeapon)) return;

            if (mainWeapon != null) weaponHandler.tryUses(playerWrapper, mainWeapon, mainStack, EquipmentSlot.HAND, TriggerType.RIGHT_CLICK);

            // Off weapon is automatically null at this point if server is using 1.8
            if (offWeapon != null) weaponHandler.tryUses(playerWrapper, offWeapon, offStack, EquipmentSlot.OFF_HAND, TriggerType.RIGHT_CLICK);
            return;
        }
        // Only do dual wield check if server is 1.9 or newer
        if (useOffHand && !weaponHandler.getInfoHandler().allowDualWielding(TriggerType.LEFT_CLICK, player, mainWeapon, offWeapon)) return;

        if (mainWeapon != null) weaponHandler.tryUses(playerWrapper, mainWeapon, mainStack, EquipmentSlot.HAND, TriggerType.LEFT_CLICK);

        // Off weapon is automatically null at this point if server is using 1.8
        if (offWeapon != null) weaponHandler.tryUses(playerWrapper, offWeapon, offStack, EquipmentSlot.OFF_HAND, TriggerType.LEFT_CLICK);
    }

    /**
     * This is simply used to cancel player arm swing animation from OTHER players.
     * It can't be cancelled from the player doing the arm swing.
     */
    @EventHandler (ignoreCancelled = true)
    public void animation(PlayerAnimationEvent e) {
        if (e.getAnimationType() != PlayerAnimationType.ARM_SWING) return;
        if (getBasicConfigurations().getBool("Disabled_Trigger_Checks.Right_And_Left_Click")) return;

        boolean useOffHand = CompatibilityAPI.getVersion() >= 1.09;

        Player player = e.getPlayer();

        // getItemInMainHand didn't exist in 1.8
        ItemStack mainStack = useOffHand ? player.getEquipment().getItemInMainHand() : player.getEquipment().getItemInHand();
        String mainWeapon = weaponHandler.getInfoHandler().getWeaponTitle(mainStack, false);

        if (mainWeapon != null && getConfigurations().getBool(mainWeapon + ".Info.Cancel.Arm_Swing_Animation")) {
            e.setCancelled(true);
            return;
        }

        // 1.8 shall not pass
        if (!useOffHand) return;

        ItemStack offStack = player.getEquipment().getItemInOffHand();
        String offWeapon = weaponHandler.getInfoHandler().getWeaponTitle(offStack, false);

        if (offWeapon != null && getConfigurations().getBool(offWeapon + ".Info.Cancel.Arm_Swing_Animation")) {
            e.setCancelled(true);
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void dropItem(PlayerDropItemEvent e) {
        if (getBasicConfigurations().getBool("Disabled_Trigger_Checks.Drop_Item")) return;
        if (getPlayerWrapper(e.getPlayer()).isInventoryOpen()) return;

        Player player = e.getPlayer();
        boolean useOffHand = CompatibilityAPI.getVersion() >= 1.09;

        ItemStack mainStack = e.getItemDrop().getItemStack();
        String mainWeapon = weaponHandler.getInfoHandler().getWeaponTitle(mainStack, false);

        // Only get off hand things is server is 1.9 or newer
        ItemStack offStack = null;
        String offWeapon = null;
        if (useOffHand) {
            offStack = player.getEquipment().getItemInOffHand();
            offWeapon = weaponHandler.getInfoHandler().getWeaponTitle(offStack, false);
        }

        if (mainWeapon == null && offWeapon == null) return;

        if (mainWeapon != null && getConfigurations().getBool(mainWeapon + ".Info.Cancel.Drop_Item", true)
                || offWeapon != null && getConfigurations().getBool(offWeapon + ".Info.Cancel.Drop_Item", true)) {
            e.setCancelled(true);

            mainStack = useOffHand ? player.getEquipment().getItemInMainHand() : player.getEquipment().getItemInHand();
        }

        IPlayerWrapper playerWrapper = getPlayerWrapper(player);

        // Only do dual wield check if server is 1.9 or newer
        if (useOffHand && !weaponHandler.getInfoHandler().allowDualWielding(TriggerType.DROP_ITEM, player, mainWeapon, offWeapon)) return;

        if (mainWeapon != null) weaponHandler.tryUses(playerWrapper, mainWeapon, mainStack, EquipmentSlot.HAND, TriggerType.DROP_ITEM);

        // Off weapon is automatically null at this point if server is using 1.8
        if (offWeapon != null) weaponHandler.tryUses(playerWrapper, offWeapon, offStack, EquipmentSlot.OFF_HAND, TriggerType.DROP_ITEM);

    }

    @EventHandler
    public void click(InventoryClickEvent e) {
        getPlayerWrapper((Player) e.getWhoClicked()).setInventoryOpen(true);
    }

    @EventHandler
    public void close(InventoryCloseEvent e) {
        getPlayerWrapper((Player) e.getPlayer()).setInventoryOpen(false);
    }
}