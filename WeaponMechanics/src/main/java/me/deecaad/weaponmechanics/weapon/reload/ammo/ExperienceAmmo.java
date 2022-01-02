package me.deecaad.weaponmechanics.weapon.reload.ammo;

import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ExperienceAmmo implements IAmmoType {

    // Defined in ammo types list
    private String ammoName;

    private String symbol;
    private int experienceAsAmmoCost;

    public ExperienceAmmo(String ammoName, String symbol, int experienceAsAmmoCost) {
        this.ammoName = ammoName;
        this.symbol = symbol;
        this.experienceAsAmmoCost = experienceAsAmmoCost;
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
    public boolean hasAmmo(IPlayerWrapper playerWrapper) {
        return playerWrapper.getPlayer().getTotalExperience() >= experienceAsAmmoCost;
    }

    @Override
    public int removeAmmo(ItemStack weaponStack, IPlayerWrapper playerWrapper, int amount, int maximumMagazineSize) {
        Player player = playerWrapper.getPlayer();
        int experience = player.getTotalExperience();
        if (experience == 0) return 0;

        int removeExperience = this.experienceAsAmmoCost * amount;

        // Check if there isn't enough experience to take
        if (removeExperience > experience) {

            // Recalculate amount to match the maximum amount that can be taken
            amount = experience / experienceAsAmmoCost;
            if (amount == 0) return 0;

            player.setTotalExperience(experience - (amount * experienceAsAmmoCost));
            return amount;
        }

        player.setTotalExperience(experience - removeExperience);
        return amount;
    }

    @Override
    public void giveAmmo(ItemStack weaponStack, IPlayerWrapper playerWrapper, int amount, int maximumMagazineSize) {
        Player player = playerWrapper.getPlayer();
        player.setTotalExperience(player.getTotalExperience() + (this.experienceAsAmmoCost * amount));
    }

    @Override
    public int getMaximumAmmo(IPlayerWrapper playerWrapper, int maximumMagazineSize) {
        int experience = playerWrapper.getPlayer().getTotalExperience();
        if (experience == 0) return 0;

        // Divide with experience cost
        return experience / experienceAsAmmoCost;
    }
}
