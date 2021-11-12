package me.deecaad.weaponmechanics.weapon.reload;

import me.deecaad.compatibility.vault.IVaultCompatibility;
import me.deecaad.compatibility.vault.VaultAPI;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.entity.Player;

public class EconomyAmmo implements IAmmoType {

    private static final IVaultCompatibility vaultCompatibility = VaultAPI.getVaultCompatibility();

    private final double moneyCost;

    public EconomyAmmo(double moneyCost) {
        this.moneyCost = moneyCost;
    }

    @Override
    public boolean hasAmmo(IEntityWrapper entityWrapper) {
        return getAmount(entityWrapper, 0) > 0;
    }

    @Override
    public int getAmount(IEntityWrapper entityWrapper, int magazineSize) {
        if (!(entityWrapper instanceof IPlayerWrapper)) return 0;
        double balance = vaultCompatibility.getBalance(((IPlayerWrapper) entityWrapper).getPlayer());
        if (balance == 0) return 0;
        return (int) (balance / moneyCost);
    }

    @Override
    public int remove(IEntityWrapper entityWrapper, int amount, int magazineSize) {
        if (!(entityWrapper instanceof IPlayerWrapper)) return 0;
        if (amount == 0) return 0;
        Player player = ((IPlayerWrapper) entityWrapper).getPlayer();
        double balance = vaultCompatibility.getBalance(player);
        if (balance == 0) return 0;

        double removeAmount = moneyCost * amount;

        if (removeAmount > balance) {
            // Meaning not enough balance
            amount = (int) (balance / moneyCost);
            if (amount == 0) return 0;
            vaultCompatibility.withdrawBalance(player, amount * moneyCost);
            return amount;
        }
        vaultCompatibility.withdrawBalance(player, removeAmount);
        return amount;
    }

    @Override
    public void give(IEntityWrapper entityWrapper, int amount, int magazineSize) {
        if (!(entityWrapper instanceof IPlayerWrapper)) return;
        if (amount == 0) return;
        vaultCompatibility.depositBalance(((IPlayerWrapper) entityWrapper).getPlayer(), moneyCost * amount);
    }
}