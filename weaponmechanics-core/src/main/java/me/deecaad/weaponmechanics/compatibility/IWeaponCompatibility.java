package me.deecaad.weaponmechanics.compatibility;

import com.cjcrafter.foliascheduler.TaskImplementation;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public interface IWeaponCompatibility {

    /**
     * Rotates player's camera rotation with given values. Absolute true means that yaw and pitch will
     * be SET to the given values. While as absolute false means that yaw and pitch is ADDED to the
     * given values.
     *
     * <p>
     * Having absolute true may cause that player's movement glitches a bit.
     *
     * @param player the player whose camera rotation to rotate
     * @param yaw absolute or relative rotation on the X axis, in degrees
     * @param pitch absolute or relative rotation on the Y axis, in degrees
     * @param absolute whether to use absolute rotation
     */
    void modifyCameraRotation(Player player, float yaw, float pitch, boolean absolute);

    /**
     * Logs "fake" damage to the given <code>victim</code>'s <code>CombatTracker</code>. This is
     * important for death messages, and any plugins that may use minecraft's built in combat tracker.
     *
     * @param victim The entity receiving the damage
     * @param source The entity giving the damage
     * @param health The health of the entity
     * @param damage The damage being applied to the entity
     * @param isMelee Whether or not this is a melee attack (And not a projectile)
     */
    void logDamage(LivingEntity victim, LivingEntity source, double health, double damage, boolean isMelee);

    /**
     * Sets which player killed the <code>victim</code>. Entities that are killed by players will drop
     * their experience.
     *
     * @param victim The entity that died
     * @param killer The killer
     */
    void setKiller(LivingEntity victim, Player killer);

    /**
     * Triggers the vanilla attack cooldown animation (item drops down then rises back up) for the given
     * player, with a duration matched to {@code durationTicks}. This is used to give visual feedback
     * during ADS settling.
     *
     * <p>Internally this resets the NMS {@code attackStrengthTicker} to 0 and temporarily modifies the
     * {@code ATTACK_SPEED} attribute so the animation completes in exactly {@code durationTicks} ticks.
     *
     * @param player       the player to show the animation to
     * @param durationTicks how many ticks the animation should take to complete
     * @return the scheduled task that will restore the attack speed attribute when the animation ends;
     *     store this in {@link me.deecaad.weaponmechanics.wrappers.ZoomData#setAdsSettleTask} so it
     *     can be cancelled if the player exits scope early
     */
    TaskImplementation<Void> playAdsSettleAnimation(Player player, int durationTicks);
}