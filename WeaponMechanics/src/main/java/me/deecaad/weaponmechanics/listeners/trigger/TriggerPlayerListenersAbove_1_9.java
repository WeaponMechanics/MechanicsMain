package me.deecaad.weaponmechanics.listeners.trigger;

import me.deecaad.weaponmechanics.weapon.WeaponHandler;
import me.deecaad.weaponmechanics.weapon.trigger.TriggerType;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import static me.deecaad.weaponmechanics.WeaponMechanics.*;

public class TriggerPlayerListenersAbove_1_9 implements Listener {

    private WeaponHandler weaponHandler;

    public TriggerPlayerListenersAbove_1_9(WeaponHandler weaponHandler) {
        this.weaponHandler = weaponHandler;
    }

    // Event priority LOW to ensure that this is ran before.
    // Weapon listeners PlayerSwapHandItemsEvent is ran.
    // Basically lower priority means that it will be one of the first EventHandlers to run.
    @EventHandler (ignoreCancelled = true, priority = EventPriority.LOW)
    public void swapHandItems(PlayerSwapHandItemsEvent e) {
        if (getBasicConfigurations().getBool("Disabled_Trigger_Checks.Swap_Main_And_Hand_Items")) return;

        Player player = e.getPlayer();
        EntityEquipment playerEquipment = player.getEquipment();

        if (player.getGameMode() == GameMode.SPECTATOR || playerEquipment == null) return;

        ItemStack toMain = e.getMainHandItem();
        String toMainWeapon = weaponHandler.getInfoHandler().getWeaponTitle(toMain, false);

        ItemStack toOff = e.getOffHandItem();
        String toOffWeapon = weaponHandler.getInfoHandler().getWeaponTitle(toOff, false);
        if (toMainWeapon == null && toOffWeapon == null) return;

        if (toMainWeapon != null && getConfigurations().getBool(toMainWeapon + ".Info.Cancel.Swap_Hands")
                || toOffWeapon != null && getConfigurations().getBool(toOffWeapon + ".Info.Cancel.Swap_Hands")) {

            e.setCancelled(true);

            toOff = playerEquipment.getItemInMainHand();
            toMain = playerEquipment.getItemInOffHand();
        }

        PlayerWrapper playerWrapper = getPlayerWrapper(player);

        boolean dualWield = toMainWeapon != null && toOffWeapon != null;

        if (isValid(toMain)) {
            // SWAP_TO_MAIN_HAND
            if (weaponHandler.getInfoHandler().denyDualWielding(TriggerType.SWAP_TO_MAIN_HAND, player, toMainWeapon, toOffWeapon)) return;

            // Only check off hand going to main hand
            if (toMainWeapon != null) weaponHandler.tryUses(playerWrapper, toMainWeapon, toMain, EquipmentSlot.OFF_HAND, TriggerType.SWAP_TO_MAIN_HAND, dualWield);
        }
        if (isValid(toOff)) {
            // SWAP_TO_OFF_HAND
            if (weaponHandler.getInfoHandler().denyDualWielding(TriggerType.SWAP_TO_OFF_HAND, player, toMainWeapon, toOffWeapon)) return;

            // Only check main hand going to off hand
            if (toOffWeapon != null) weaponHandler.tryUses(playerWrapper, toOffWeapon, toOff, EquipmentSlot.HAND, TriggerType.SWAP_TO_OFF_HAND, dualWield);
        }
    }

    private boolean isValid(ItemStack itemStack) {
        return itemStack != null && itemStack.getType() != Material.AIR;
    }
}