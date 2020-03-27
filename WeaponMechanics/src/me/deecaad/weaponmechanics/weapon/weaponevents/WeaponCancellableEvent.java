package me.deecaad.weaponmechanics.weapon.weaponevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;

public class WeaponCancellableEvent extends WeaponEvent implements Cancellable {

    private boolean cancel;

    /**
     * Called when any cancellable weapon event is called.
     *
     * @param weaponTitle the weapon title used in event
     */
    public WeaponCancellableEvent(String weaponTitle, LivingEntity livingEntity) {
        super(weaponTitle, livingEntity);
    }

    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
