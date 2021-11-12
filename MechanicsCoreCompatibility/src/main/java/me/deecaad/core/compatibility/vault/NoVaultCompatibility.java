package me.deecaad.core.compatibility.vault;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class NoVaultCompatibility implements IVaultCompatibility {

    @Override
    public double getBalance(OfflinePlayer player) {
        return 0;
    }

    @Override
    public void setBalance(OfflinePlayer player, double balance) {
    }

    @Override
    public boolean hasBalance(OfflinePlayer player, double amount) {
        return false;
    }

    @Override
    public void withdrawBalance(OfflinePlayer player, double amount) {
    }

    @Override
    public void depositBalance(OfflinePlayer player, double amount) {
    }

    @Override
    public String getPrefix(Player player) {
        return null;
    }

    @Override
    public String getSuffix(Player player) {
        return null;
    }

    @Override
    public String[] getGroups() {
        return new String[0];
    }

    @Override
    public String[] getGroups(Player player) {
        return new String[0];
    }

    @Override
    public String getGroup(Player player) {
        return null;
    }
}