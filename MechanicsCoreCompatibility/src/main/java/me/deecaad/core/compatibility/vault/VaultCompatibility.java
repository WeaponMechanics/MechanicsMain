package me.deecaad.core.compatibility.vault;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.utils.Debugger;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.plugin.ServicesManager;

public class VaultCompatibility implements IVaultCompatibility, Listener {

    private Chat chat;
    private Permission permission;
    private Economy economy;

    public VaultCompatibility() {
        refreshVault();

        Bukkit.getPluginManager().registerEvents(this, MechanicsCore.getPlugin());
    }

    private void refreshVault() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null)
            throw new IllegalStateException("Vault is not installed or is not loaded. Instead use NoVaultCompatibility");

        Debugger debug = MechanicsCore.debug;

        ServicesManager services = Bukkit.getServicesManager();
        Chat chat = services.load(Chat.class);
        Permission permission = services.load(Permission.class);
        Economy economy = services.load(Economy.class);

        if (this.chat != chat)
            debug.debug("Registered new Vault chat: " + (chat == null ? "null" : chat.getName()));
        if (this.permission != permission)
            debug.debug("Registered new Vault permission: " + (permission == null ? "null" : permission.getName()));
        if (this.economy != economy)
            debug.debug("Registered new Vault economy: " + (economy == null ? "null" : economy.getName()));

        this.chat = chat;
        this.permission = permission;
        this.economy = economy;
    }

    @EventHandler
    public void onServiceChange(final ServiceRegisterEvent e) {
        Class<?> service = e.getProvider().getService();

        if (service == Chat.class || service == Permission.class || service == Economy.class)
            refreshVault();
    }

    @EventHandler
    public void onServiceChange(final ServiceUnregisterEvent e) {
        Class<?> service = e.getProvider().getService();

        if (service == Chat.class || service == Permission.class || service == Economy.class)
            refreshVault();
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        if (economy == null) {
            return 0.0;
        } else if (!economy.hasAccount(player)) {
            economy.createPlayerAccount(player);
        }

        return economy.getBalance(player);
    }

    @Override
    public void setBalance(OfflinePlayer player, double balance) {
        if (economy != null) {
            if (!economy.hasAccount(player)) {
                economy.createPlayerAccount(player);
            }

            double current = economy.getBalance(player);

            if (balance > current) {
                economy.depositPlayer(player, balance - current);
            } else {
                economy.withdrawPlayer(player, current - balance);
            }
        }
    }

    @Override
    public boolean hasBalance(OfflinePlayer player, double amount) {
        if (economy == null) {
            return true;
        }

        return economy.has(player, amount);
    }

    @Override
    public void withdrawBalance(OfflinePlayer player, double amount) {
        economy.withdrawPlayer(player, amount);
    }

    @Override
    public void depositBalance(OfflinePlayer player, double amount) {
        economy.depositPlayer(player, amount);
    }

    @Override
    public String getPrefix(Player player) {
        return chat.getPlayerPrefix(player);
    }

    @Override
    public String getSuffix(Player player) {
        return chat.getPlayerSuffix(player);
    }

    @Override
    public String[] getGroups() {
        return permission.getGroups();
    }

    @Override
    public String[] getGroups(Player player) {
        return permission.getPlayerGroups(player);
    }

    @Override
    public String getGroup(Player player) {
        return permission.getPrimaryGroup(player);
    }
}