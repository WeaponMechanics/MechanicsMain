package me.deecaad.weaponmechanics.wrappers;

import me.deecaad.weaponmechanics.general.ColorType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
     * @param standing whether or not to stand
     */
    void setStanding(boolean standing);

    /**
     * @return true if entity is walking
     */
    boolean isWalking();

    /**
     * @param walking whether or not to walk
     */
    void setWalking(boolean walking);

    /**
     * @return true if entity is in midair
     */
    boolean isInMidair();

    /**
     * @param inMidair whether or not to be in midair
     */
    void setInMidair(boolean inMidair);

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
     * @return true if entity is reloading
     */
    boolean isReloading();

    /**
     * @return true if entity is swimming
     */
    boolean isSwimming();

    /**
     * @return true if entity is gliding
     */
    boolean isGliding();
    
    /**
     *
     * @param player The player who is scoping
     * @return The color this entity should be glowing
     */
    @Nullable
    ColorType getThermalColor(@Nullable Player player);
}