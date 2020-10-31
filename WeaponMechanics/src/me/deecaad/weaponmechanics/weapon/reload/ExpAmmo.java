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
    public boolean hasAmmo(IEntityWrapper entityWrapper) {
        return getAmount(entityWrapper, 0) > 0;
    }

    @Override
    public int getAmount(IEntityWrapper entityWrapper, int magazineSize) {
        if (!(entityWrapper instanceof IPlayerWrapper)) return 0;

        return ((Player) entityWrapper.getEntity()).getTotalExperience() / expCost;
    }

    @Override
    public int remove(IEntityWrapper entityWrapper, int amount, int magazineSize) {
        if (!(entityWrapper instanceof IPlayerWrapper)) return 0;
        if (amount == 0) return 0;
        Player player = (Player) entityWrapper.getEntity();
        int removeAmount = expCost * amount;
        int totalExp = player.getTotalExperience();

        int setExp = totalExp - removeAmount;

        if (setExp < 0) {
            // Meaning not enough ammo
            amount = totalExp / expCost;
            if (amount == 0) return 0;
            player.setTotalExperience(totalExp - (amount * expCost));
            return amount;
        }
        player.setTotalExperience(setExp);
        return amount;
    }

    @Override
    public void give(IEntityWrapper entityWrapper, int amount, int magazineSize) {
        if (!(entityWrapper instanceof IPlayerWrapper)) return;
        if (amount == 0) return;
        Player player = (Player) entityWrapper.getEntity();
        player.setTotalExperience(player.getTotalExperience() + (expCost * amount));
    }
}
