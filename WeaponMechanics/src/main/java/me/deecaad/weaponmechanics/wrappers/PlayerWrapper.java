package me.deecaad.weaponmechanics.wrappers;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.utils.NumberUtil;
import org.bukkit.entity.Player;

/**
 * Wraps a {@link Player} object to simplify per-player data/methods that
 * are used by WeaponMechanics. Also contains useful API functionality for
 * plugins who want to check if an entity is scoped, reloading, etc.
 */
public class PlayerWrapper extends EntityWrapper {

    private final Player player;
    private long lastRightClick;
    private long lastStartSneak;
    private long lastWeaponDrop;
    private MessageHelper messageHelper;
    private long lastAmmoConvert;

    public PlayerWrapper(Player player) {
        super(player);
        this.player = player;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void rightClicked() {
        lastRightClick = System.currentTimeMillis();
    }

    public boolean didDoubleSneak() {
        if (lastStartSneak == 0) {
            lastStartSneak = System.currentTimeMillis();

            // There hasn't yet been last sneak -> false
            return false;
        }

        if (!NumberUtil.hasMillisPassed(lastStartSneak, 500)) {
            // Double sneaked
            lastStartSneak = 0; // Reset the timer
            return true;
        }

        lastStartSneak = System.currentTimeMillis();
        return false;
    }

    public void droppedWeapon() {
        lastWeaponDrop = System.currentTimeMillis();
    }

    public long getLastDropWeaponTime() {
        return lastWeaponDrop;
    }

    public MessageHelper getMessageHelper() {
        return messageHelper == null ? messageHelper = new MessageHelper() : messageHelper;
    }

    public void convertedAmmo() {
        lastAmmoConvert = System.currentTimeMillis();
    }

    public long getLastAmmoConvert() {
        return lastAmmoConvert;
    }

    @Override
    public boolean isRightClicking() {

        // When a player is blocking with a shield or a sword, then they are
        // definitely right-clicking.
        if (player.isBlocking())
            return true;

        int ping = CompatibilityAPI.getCompatibility().getPing(player);
        if (ping > 215) {
            // Ping was more than 215 so lets take player's ping in account
            // when checking if it is still right clicking
            return !NumberUtil.hasMillisPassed(lastRightClick, ping + 15);
        }
        return !NumberUtil.hasMillisPassed(lastRightClick, 215);
    }

    @Override
    public boolean isSneaking() {
        return player.isSneaking();
    }

    @Override
    public boolean isSprinting() {
        return player.isSprinting();
    }

    @Override
    public boolean isPlayer() {
        return true;
    }
}
