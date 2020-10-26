package me.deecaad.weaponmechanics.weapon.reload;

import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.entity.Player;

public class ExpAmmo implements IAmmoType {

    private final int expCost;

    public ExpAmmo(int expCost) {
        this.expCost = expCost;
    }

    @Override
    public int getAmount(IEntityWrapper entityWrapper) {
        if (!(entityWrapper instanceof IPlayerWrapper)) return 0;

        return ((Player) entityWrapper.getEntity()).getTotalExperience() / expCost;
    }

    @Override
    public int remove(IEntityWrapper entityWrapper, int amount) {
        if (!(entityWrapper instanceof IPlayerWrapper)) return 0;
        Player player = (Player) entityWrapper.getEntity();
        int removeAmount = expCost * amount;
        int totalExp = player.getTotalExperience();

        int setExp = totalExp - removeAmount;

        if (setExp < 0) {
            // Meaning not enough ammo
            amount = totalExp / expCost;
            player.setTotalExperience(totalExp - (amount * expCost));
            return amount;
        }
        player.setTotalExperience(setExp);
        return amount;
    }

    @Override
    public void give(IEntityWrapper entityWrapper, int amount) {
        if (!(entityWrapper instanceof IPlayerWrapper)) return;
        Player player = (Player) entityWrapper.getEntity();
        player.setTotalExperience(player.getTotalExperience() + (expCost * amount));
    }
}
