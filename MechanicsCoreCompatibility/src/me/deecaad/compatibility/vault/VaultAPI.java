package me.deecaad.compatibility.vault;

import org.bukkit.Bukkit;

public class VaultAPI {

    private static final IVaultCompatibility COMPATIBILITY;

    static {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            COMPATIBILITY = new VaultCompatibility();
        } else {
            COMPATIBILITY = new NoVaultCompatibility();
        }
    }

    public static IVaultCompatibility getVaultCompatibility() {
        return COMPATIBILITY;
    }
}