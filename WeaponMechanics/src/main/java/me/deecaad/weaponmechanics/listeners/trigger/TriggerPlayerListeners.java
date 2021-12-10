package me.deecaad.weaponmechanics.listeners.trigger;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.events.PlayerJumpEvent;
import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import static me.deecaad.weaponmechanics.WeaponMechanics.*;

public class TriggerPlayerListeners implements Listener {

    private WeaponHandler weaponHandler;

    public TriggerPlayerListeners(WeaponHandler weaponHandler) {
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

        Player player = e.getPlayer();
        boolean isSneaking = e.isSneaking();

        if (isSneaking && getPlayerWrapper(player).didDoubleSneak()) {
            weaponHandler.useTrigger(player, TriggerType.DOUBLE_SNEAK, false);
            // DOUBLE_SNEAK and START_SNEAK can be called nearly at same time
        }

        weaponHandler.useTrigger(player, isSneaking ? TriggerType.START_SNEAK : TriggerType.END_SNEAK, false);
    }

    @EventHandler (ignoreCancelled = true)
    public void toggleSprint(PlayerToggleSprintEvent e) {
        if (getBasicConfigurations().getBool("Disabled_Trigger_Checks.Sprint")) return;

        weaponHandler.useTrigger(e.getPlayer(), e.isSprinting() ? TriggerType.START_SPRINT : TriggerType.END_SPRINT, false);
    }

    @EventHandler
    public void jump(PlayerJumpEvent e) {
        // Whether this is used its checked already in MoveTask class
        weaponHandler.useTrigger(e.getPlayer(), e.isDoubleJump() ? TriggerType.DOUBLE_JUMP : TriggerType.JUMP, false);
    }

    @EventHandler (ignoreCancelled = true)
    public void toggleFlight(PlayerToggleFlightEvent e) {
        if (getBasicConfigurations().getBool("Disabled_Trigger_Checks.Double_Jump")) return;

        Player player = e.getPlayer();
        GameMode gameMode = player.getGameMode();
        if (gameMode == GameMode.SURVIVAL || gameMode == GameMode.ADVENTURE) {
            e.setCancelled(true);
            player.setFlying(false);
            player.setAllowFlight(false);
            Bukkit.getPluginManager().callEvent(new PlayerJumpEvent(player, true));
        }
    }

    @EventHandler
    public void interact(PlayerInteractEvent e) {
        Action action = e.getAction();
        Player player = e.getPlayer();

        // I don't think ignoreCancelled = true works in this event properly
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (action == Action.PHYSICAL || e.useItemInHand() == Event.Result.DENY) return;
        if (getBasicConfigurations().getBool("Disabled_Trigger_Checks.Right_And_Left_Click")) return;

        boolean useOffHand = CompatibilityAPI.getVersion() >= 1.09;

        // Basically this just cancel double call to player interact event
        if (useOffHand) {
            EquipmentSlot hand = e.getHand();

            // Only if main hand is air (off hand can be whatever
            if (player.getEquipment().getItemInMainHand().getType() == Material.AIR) {

                // Check if the action was right click block AND hand used was main hand
                // -> Cancel
                if (action == Action.RIGHT_CLICK_BLOCK && hand == EquipmentSlot.HAND) {
                    return;
                }
                // This basically means that hand is now OFF_HAND, but it doesn't let HAND calls pass

            } else if (hand == EquipmentSlot.OFF_HAND) {
                // If main hand had item, then we can always just cancel OFF_HAND call since HAND is guaranteed to be used
                return;
            }
        }

        IPlayerWrapper playerWrapper = getPlayerWrapper(player);

        boolean rightClick = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
        if (rightClick) {
            getPlayerWrapper(player).rightClicked();
        } else if (CompatibilityAPI.getVersion() >= 1.15 && !NumberUtil.hasMillisPassed(playerWrapper.getLastDropWeaponTime(), 25)) {
            // Fixes bug where item dropping causes player to left click
            // Basically checks if less than 25 millis has passed since weapon item drop
            return;
        }

        // 1.8 support...
        ItemStack mainStack = useOffHand ? player.getEquipment().getItemInMainHand() : player.getEquipment().getItemInHand();

        String mainWeapon = weaponHandler.getInfoHandler().getWeaponTitle(mainStack, true);

        // Only get off hand things is server is 1.9 or newer
        ItemStack offStack = null;
        String offWeapon = null;
        if (useOffHand) {
            offStack = player.getEquipment().getItemInOffHand();
            offWeapon = weaponHandler.getInfoHandler().getWeaponTitle(offStack, true);
        }

        if (mainWeapon == null && offWeapon == null) return;

        boolean isBlockInteraction = action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK;
        if (isBlockInteraction && (mainWeapon != null && getConfigurations().getBool(mainWeapon + ".Info.Cancel.Block_Interactions", true)
                || offWeapon != null && getConfigurations().getBool(offWeapon + ".Info.Cancel.Block_Interactions", true))) {
            e.setUseInteractedBlock(Event.Result.DENY);
        }

        if (!isBlockInteraction && (mainWeapon != null && getConfigurations().getBool(mainWeapon + ".Info.Cancel.Item_Interactions")
                || offWeapon != null && getConfigurations().getBool(offWeapon + ".Info.Cancel.Item_Interactions"))) {
            e.setUseItemInHand(Event.Result.DENY);
        }

        boolean dualWield = mainWeapon != null && offWeapon != null;

        if (rightClick) {
            // Only do dual wield check if server is 1.9 or newer
            if (useOffHand && weaponHandler.getInfoHandler().denyDualWielding(TriggerType.RIGHT_CLICK, player, mainWeapon, offWeapon)) return;

            if (mainWeapon != null) weaponHandler.tryUses(playerWrapper, mainWeapon, mainStack, EquipmentSlot.HAND, TriggerType.RIGHT_CLICK, dualWield);

            // Off weapon is automatically null at this point if server is using 1.8
            if (offWeapon != null) weaponHandler.tryUses(playerWrapper, offWeapon, offStack, EquipmentSlot.OFF_HAND, TriggerType.RIGHT_CLICK, dualWield);
            return;
        }

        // Only do dual wield check if server is 1.9 or newer
        if (useOffHand && weaponHandler.getInfoHandler().denyDualWielding(TriggerType.LEFT_CLICK, player, mainWeapon, offWeapon)) return;

        if (mainWeapon != null) weaponHandler.tryUses(playerWrapper, mainWeapon, mainStack, EquipmentSlot.HAND, TriggerType.LEFT_CLICK, dualWield);

        // Off weapon is automatically null at this point if server is using 1.8
        if (offWeapon != null) weaponHandler.tryUses(playerWrapper, offWeapon, offStack, EquipmentSlot.OFF_HAND, TriggerType.LEFT_CLICK, dualWield);
    }

    /**
     * This is simply used to cancel player arm swing animation from OTHER players.
     * It can't be cancelled from the player doing the arm swing.
     */
    @EventHandler (ignoreCancelled = true)
    public void animation(PlayerAnimationEvent e) {
        if (e.getAnimationType() != PlayerAnimationType.ARM_SWING) return;
        if (getBasicConfigurations().getBool("Disabled_Trigger_Checks.Right_And_Left_Click")) return;

        Player player = e.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        IPlayerWrapper playerWrapper = getPlayerWrapper(player);

        double version = CompatibilityAPI.getVersion();
        if (version >= 1.15 && !NumberUtil.hasMillisPassed(playerWrapper.getLastDropWeaponTime(), 25)) {
            e.setCancelled(true);
            return;
        }

        boolean useOffHand = version >= 1.09;

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
        Player player = e.getPlayer();

        if (getBasicConfigurations().getBool("Disabled_Trigger_Checks.Drop_Item")) return;

        IPlayerWrapper playerWrapper = getPlayerWrapper(player);

        if (playerWrapper.isInventoryOpen()) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;

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

        if (playerWrapper.getMainHandData().isReloading() || playerWrapper.getOffHandData().isReloading()) {
            // Cancel reload (and other tasks) since drop item will most of the time cause
            // itemstack reference change which will cause other bugs (e.g. infinite reload bug)
            playerWrapper.getMainHandData().cancelTasks();
            playerWrapper.getOffHandData().cancelTasks();
        }

        if (mainWeapon != null && getConfigurations().getBool(mainWeapon + ".Info.Cancel.Drop_Item", true)
                || offWeapon != null && getConfigurations().getBool(offWeapon + ".Info.Cancel.Drop_Item", true)) {

            e.setCancelled(true);
        }

        // Only do dual wield check if server is 1.9 or newer
        if (useOffHand && weaponHandler.getInfoHandler().denyDualWielding(TriggerType.DROP_ITEM, player, mainWeapon, offWeapon)) return;

        boolean dualWield = mainWeapon != null && offWeapon != null;

        if (mainWeapon != null) {
            playerWrapper.droppedWeapon();

            // This due to sometimes the instance changes in item drop...
            // - 1 item in slot when dropping -> reference changes
            // - 2 items or more in slot when dropping -> reference stays same
            Bukkit.getScheduler().runTask(WeaponMechanics.getPlugin(), () -> weaponHandler.tryUses(playerWrapper, mainWeapon,
                    useOffHand ? player.getEquipment().getItemInMainHand() : player.getEquipment().getItemInHand(),
                    EquipmentSlot.HAND, TriggerType.DROP_ITEM, dualWield));
        }

        // Off weapon is automatically null at this point if server is using 1.8
        if (offWeapon != null) {
            playerWrapper.droppedWeapon();
            weaponHandler.tryUses(playerWrapper, offWeapon, offStack, EquipmentSlot.OFF_HAND, TriggerType.DROP_ITEM, dualWield);
        }
    }

    @EventHandler
    public void open(InventoryOpenEvent e) {
        getPlayerWrapper((Player) e.getPlayer()).setInventoryOpen(true);
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