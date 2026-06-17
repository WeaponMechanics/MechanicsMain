package me.deecaad.weaponmechanics.spike;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.MechanicsLogger;

import java.util.concurrent.CompletableFuture;

/**
 * SPIKE: MechanicsCore for tests. The real onLoad() calls CommandAPI.onLoad() (server-version
 * detection that MockBukkit fails) before its own logger init, so we replace onLoad with a minimal
 * version that only sets the debugger. onEnable is skipped entirely (metrics/commands/registries
 * aren't needed: MechanicSerializer resolves built-in mechanics via `new GlobalSymbolSource()`).
 * INSTANCE is set by MechanicsCore's constructor, so getInstance() works regardless.
 */
public class TestMechanicsCore extends MechanicsCore {

    @Override
    public void onLoad() {
        setDebugger(new MechanicsLogger(this, new MechanicsLogger.LoggerConfig()));
    }

    @Override
    public void onEnable() {
        // skip CommandAPI command registration, bStats metrics, jar-scan config load
    }

    @Override
    public CompletableFuture<Void> handleMetrics() {
        return CompletableFuture.completedFuture(null);
    }
}
