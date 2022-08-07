package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmAction;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmState;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class WeaponFirearmEvent extends WeaponEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final FirearmAction action;
    private final FirearmState state;
    private Mechanics mechanics;
    private int time;

    private boolean cancelled;

    public WeaponFirearmEvent(String weaponTitle, ItemStack weaponStack, LivingEntity shooter, FirearmAction action, FirearmState state) {
        super(weaponTitle, weaponStack, shooter);
        this.action = action;
        this.state = state;

        time = -1;
    }

    public FirearmAction getAction() {
        return action;
    }

    public FirearmType getType() {
        return action.getFirearmType();
    }

    public FirearmState getState() {
        return state;
    }

    public Mechanics getMechanics() {
        if (mechanics == null)
            return state == FirearmState.CLOSE ? action.getClose() : action.getOpen();

        return mechanics;
    }

    public void setMechanics(Mechanics mechanics) {
        this.mechanics = mechanics;
    }

    public int getTime() {
        if (time == -1)
            return state == FirearmState.CLOSE ? action.getCloseTime() : action.getOpenTime();

        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public void useMechanics(CastData castData, boolean isOpen) {
        if (isOpen) {
            if (mechanics != null) mechanics.use(castData);
            else if (action.getOpen() != null) action.getOpen().use(castData);
        } else {
            if (mechanics != null) mechanics.use(castData);
            else if (action.getClose() != null) action.getClose().use(castData);
        }
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
