package me.deecaad.weaponmechanics.weapon.reload.ammo;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.vault.IVaultCompatibility;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MoneyAmmo implements IAmmoType {

    private static final IVaultCompatibility vaultCompatibility = CompatibilityAPI.getVaultCompatibility();

    // Defined in ammo types list
    private String ammoName;

    private String symbol;
    private double moneyAsAmmoCost;

    public MoneyAmmo(String ammoName, String symbol, double moneyAsAmmoCost) {
        this.ammoName = ammoName;
        this.symbol = symbol;
        this.moneyAsAmmoCost = moneyAsAmmoCost;
    }

    @Override
    public String getAmmoName() {
        return ammoName;
    }

    @Override
    public String getSymbol() {
        return symbol != null ? symbol : ammoName;
    }

    @Override
    public boolean hasAmmo(PlayerWrapper playerWrapper) {
        return vaultCompatibility.getBalance(playerWrapper.getPlayer()) >= moneyAsAmmoCost;
    }

    @Override
    public int removeAmmo(ItemStack weaponStack, PlayerWrapper playerWrapper, int amount, int maximumMagazineSize) {
        if (amount == 0) return 0;
        Player player = playerWrapper.getPlayer();
        double balance = vaultCompatibility.getBalance(player);
        if (balance == 0) return 0;

        double removeMoney = this.moneyAsAmmoCost * amount;

        // Check if there isn't enough money to withdraw
        if (removeMoney > balance) {

            // Recalculate amount to match the maximum amount that can be withdrawed
            amount = (int) (balance / moneyAsAmmoCost);
            if (amount == 0) return 0;

            vaultCompatibility.withdrawBalance(player, amount * moneyAsAmmoCost);
            return amount;
        }

        vaultCompatibility.withdrawBalance(player, removeMoney);
        return amount;
    }

    @Override
    public void giveAmmo(ItemStack weaponStack, PlayerWrapper playerWrapper, int amount, int maximumMagazineSize) {
        if (amount == 0) return;
        vaultCompatibility.depositBalance(playerWrapper.getPlayer(), this.moneyAsAmmoCost * amount);
    }

    @Override
    public int getMaximumAmmo(PlayerWrapper playerWrapper, int maximumMagazineSize) {
        double balance = vaultCompatibility.getBalance(playerWrapper.getPlayer());
        if (balance == 0) return 0;

        // Divide with money cost and convert to int
        return (int) (balance / moneyAsAmmoCost);
    }
}