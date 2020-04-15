package me.deecaad.weaponmechanics.wrappers;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;

import javax.annotation.Nonnull;

/**
 * This class wraps an Entity. Contains
 * basic information for this plugin for
 * each LivingEntity
 *
 * @since 1.0
 */
public interface IEntityWrapper {

    /**
     * @return the living entity held by entity wrapper
     */
    LivingEntity getEntity();

    /**
     * @return the move task bukkit runnable task id
     */
    int getMoveTask();

    /**
     * @return true if entity is standing
     */
    boolean isStanding();

    /**
     * Automatically sets walking to false and calls events
     *
     * @param standing whether or not to stand
     */
    void setStanding(boolean standing);

    /**
     * @return true if entity is walking
     */
    boolean isWalking();

    /**
     * Automatically sets standing to false and calls events
     *
     * @param walking whether or not to walk
     */
    void setWalking(boolean walking);

    /**
     * Automatically calls event
     *
     * @return true if entity is in midair
     */
    boolean isInMidair();

    /**
     * Automatically calls event
     *
     * @param inMidair whether or not to be in midair
     */
    void setInMidair(boolean inMidair);

    /**
     * @return true if entity is swimming
     */
    boolean isSwimming();

    /**
     * @param swimming whether or not to swim
     */
    void setSwimming(boolean swimming);

    /**
     * @return true if entity is zooming
     */
    boolean isZooming();

    /**
     * @return the zoom data
     */
    @Nonnull
    ZoomData getZoomData();

    /**
     * @return the spread change
     */
    @Nonnull
    SpreadChange getSpreadChange();

    /**
     * @return true if entity is sneaking
     */
    boolean isSneaking();

    /**
     * @return true if entity is sprinting
     */
    boolean isSprinting();

    /**
     * @return true if entity is gliding
     */
    boolean isGliding();

    /**
     * Used to check whether or not entity is currently reloading with specific hand
     *
     * @param slot the hand to check
     * @return true if entity is reloading
     */
    boolean isReloading(EquipmentSlot slot);

    /**
     * This is only accurate for swords(1.8) and shields(1.9 and above).
     *
     * Otherwise the right click detection is determined by time between last right click
     * and time when this method is called.
     *
     * Right click event is only called every 195-215 millis
     * so this isn't fully accurate. Basically that means that right click
     * detection is about 4 ticks accurate.
     *
     * This also takes player's ping in account. If ping is more than 215 then
     * millis passed check is done with (player ping + 15). Otherwise millis
     * passed check is done with 215 millis.
     *
     * @return true if player may be right clicking
     */
    boolean isRightClicking();

    /**
     * @param equipmentSlot the hand to set
     * @param usingFullAuto whether or not using full auto
     */
    void setUsingFullAuto(EquipmentSlot equipmentSlot, boolean usingFullAuto);

    /**
     * Used to check whether or not entity is using full auto CURRENTLY
     *
     * @param equipmentSlot the hand to check
     * @return true if entity is using full auto currently
     */
    boolean isUsingFullAuto(EquipmentSlot equipmentSlot);

    /**
     * Sets delay for given hand at current time millis
     *
     * @param equipmentSlot the hand to set
     */
    void setDelayBetweenShots(EquipmentSlot equipmentSlot);

    /**
     * @param equipmentSlot the hand to check
     * @param delayInMillis the delay required to be passed
     * @return true if delay is over
     */
    boolean hasDelayBetweenShots(EquipmentSlot equipmentSlot, long delayInMillis);
}