package me.deecaad.core.compatibility.vault;

import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Bukkit;

public class VaultAPI {

    private static final IVaultCompatibility COMPATIBILITY;

    static {
        boolean hasVault = Bukkit.getPluginManager().getPlugin("Vault") != null;
        Class<IVaultCompatibility> vaultClass = ReflectionUtil.getClass(hasVault ? "VaultCompatibility" : "NoVaultCompatibility");
        COMPATIBILITY = ReflectionUtil.newInstance(vaultClass);
    }

    public static IVaultCompatibility getVaultCompatibility() {
        return COMPATIBILITY;
    }
}