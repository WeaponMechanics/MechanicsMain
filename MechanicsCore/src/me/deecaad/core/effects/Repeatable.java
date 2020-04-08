package me.deecaad.core.effects;

/**
 * This interface makes some repeatable, which means
 * the action can repeat multiple times with a delay,
 * or interval between repetitions
 */
public interface Repeatable {

    /**
     * The repeat amount represents how many times the
     * effect should spawn itself.
     *
     * @return How many times to repeat the effect
     */
    int getRepeatAmount();

    /**
     * Sets the number of times for an action to
     * repeat itself
     *
     * @param repeatAmount Number of repetitions
     */
    void setRepeatAmount(int repeatAmount);

    /**
     * Gets the interval, or time in between repetitions.
     * The interval should be given in ticks (20 = 1 second)
     *
     * @return The time between repetitions
     */
    int getRepeatInterval();

    /**
     * Sets the interval, or time in between repetitions.
     * The interval should be given in ticks (20 = 1 second)
     *
     * @param repeatInterval Time between repetitions
     */
    void setRepeatInterval(int repeatInterval);
}
