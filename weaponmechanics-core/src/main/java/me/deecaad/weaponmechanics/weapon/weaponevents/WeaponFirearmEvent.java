package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.core.mechanics.scope.CastScope;
import me.deecaad.core.mechanics.scope.Value;
import me.deecaad.core.mechanics.program.Program;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmAction;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmState;
import me.deecaad.weaponmechanics.weapon.firearm.FirearmType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * When a weapon's {@link FirearmState} changes.
 */
public class WeaponFirearmEvent extends WeaponEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final FirearmAction action;
    private final FirearmState state;
    private Program mechanics;
    private int time;

    public WeaponFirearmEvent(String weaponTitle, ItemStack weaponStack, LivingEntity shooter, EquipmentSlot hand, FirearmAction action, FirearmState state) {
        super(weaponTitle, weaponStack, shooter, hand);
        this.action = action;
        this.state = state;

        time = -1;
    }

    /**
     * The config options of the firearm. You probably do not want to modify this value.
     *
     * @return The firearm config options.
     */
    public FirearmAction getAction() {
        return action;
    }

    /**
     * Returns the firearm type.
     *
     * @return The firearm type.
     */
    public FirearmType getType() {
        return action.getFirearmType();
    }

    /**
     * Returns whether the state is OPEN, CLOSE, or READY.
     *
     * @return The firearm state.
     */
    public FirearmState getState() {
        return state;
    }

    /**
     * The mechanics that will be used (usually for sounds).
     *
     * @return The mechanics that will be played after the event.
     */
    public Program getMechanics() {
        if (mechanics == null)
            return state == FirearmState.CLOSE ? action.getClose() : action.getOpen();

        return mechanics;
    }

    /**
     * The mechanics that will be used (usually for sounds).
     *
     * @param mechanics The mechanics that will be played after the event.
     */
    public void setMechanics(Program mechanics) {
        this.mechanics = mechanics;
    }

    /**
     * Returns how long it takes for the firearm action to be completed.
     *
     * @return The firearm action time.
     */
    public int getTime() {
        if (time == -1)
            return state == FirearmState.CLOSE ? action.getCloseTime() : action.getOpenTime();

        return time;
    }

    /**
     * Sets how long it takes for the firearm action to be completed.
     *
     * @param time The firearm action time.
     */
    public void setTime(int time) {
        this.time = time;
    }

    public void useMechanics(CastScope castData, boolean isOpen) {
        castData.setVariable("firearm_state", Value.of(isOpen ? "OPEN" : "CLOSE"));
        if (getType() != null)
            castData.setVariable("firearm_action", Value.of(getType().name()));
        if (isOpen) {
            if (mechanics != null)
                mechanics.run(castData);
            else if (action.getOpen() != null)
                action.getOpen().run(castData);
        } else {
            if (mechanics != null)
                mechanics.run(castData);
            else if (action.getClose() != null)
                action.getClose().run(castData);
        }
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
