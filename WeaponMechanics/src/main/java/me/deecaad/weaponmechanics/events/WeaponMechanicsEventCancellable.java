package me.deecaad.weaponmechanics.events;

import org.bukkit.event.Cancellable;

public class WeaponMechanicsEventCancellable extends WeaponMechanicsEvent implements Cancellable {

    private boolean cancel;

    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
