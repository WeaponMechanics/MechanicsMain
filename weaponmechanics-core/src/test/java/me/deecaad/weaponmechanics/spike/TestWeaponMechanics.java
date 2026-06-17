package me.deecaad.weaponmechanics.spike;

import me.deecaad.core.MechanicsLogger;
import me.deecaad.core.file.FastConfiguration;
import me.deecaad.weaponmechanics.WeaponMechanics;

import java.util.concurrent.CompletableFuture;

/**
 * SPIKE: WeaponMechanics for tests. We DON'T call super.onLoad() (it runs handleFiles/jar-scan).
 * Instead we set INSTANCE (via the real onLoad's first action is INSTANCE=this — but that's private,
 * so we rely on super.onLoad being skipped and set what serializers actually need: debugger + config).
 *
 * NOTE: WeaponMechanics.INSTANCE is private and only set inside WeaponMechanics.onLoad(). To get a
 * non-null getInstance() we must let onLoad run far enough to set it. We call super.onLoad() but it
 * tolerates the missing jar (its mechanic jar-scan is wrapped in try/catch).
 */
public class TestWeaponMechanics extends WeaponMechanics {

    @Override
    public void onLoad() {
        // super.onLoad() sets INSTANCE=this, builds the debugger, and attempts the (gracefully
        // failing) jar scan for WM mechanics/conditions. It also calls MechanicsPlugin.onLoad ->
        // handleFiles() which writes+loads default configs into the mock data folder.
        super.onLoad();

        // Guarantee a non-null configuration for serializers that read flags
        // (e.g. RelativeSkin's "Strict_Relative_Skins"); harmless if onLoad already set one.
        if (!isConfigurationReady())
            setConfiguration(new FastConfiguration());
    }

    @Override
    public void onEnable() {
        // skip metrics/commands/jar-scan config load
    }

    @Override
    public CompletableFuture<Void> handleMetrics() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> handleConfigs() {
        return CompletableFuture.completedFuture(null);
    }

    private boolean isConfigurationReady() {
        try {
            return getConfiguration() != null;
        } catch (Throwable lateinitNotSet) {
            return false;
        }
    }
}
