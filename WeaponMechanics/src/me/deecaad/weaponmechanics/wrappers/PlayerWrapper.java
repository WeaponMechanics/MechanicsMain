package me.deecaad.weaponmechanics.wrappers;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.weaponmechanics.general.ColorType;
import me.deecaad.core.utils.NumberUtils;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class PlayerWrapper extends EntityWrapper implements IPlayerWrapper {

    public Map<Player, ColorType> thermalScopeData;
    private final Player player;
    private boolean denyNextSetSlotPacket;
    private boolean inventoryOpen;
    private long lastRightClick;

    public PlayerWrapper(Player player) {
        super(player);

        this.player = player;
        thermalScopeData = new HashMap<>();
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public boolean isDenyNextSetSlotPacket() {
        return denyNextSetSlotPacket;
    }

    @Override
    public void setDenyNextSetSlotPacket(boolean denyNext) {
        this.denyNextSetSlotPacket = denyNext;
    }

    @Override
    public boolean isInventoryOpen() {
        return inventoryOpen;
    }

    @Override
    public void setInventoryOpen(boolean isOpen) {
        this.inventoryOpen = isOpen;
    }

    @Override
    public void rightClicked() {
        lastRightClick = System.currentTimeMillis();
    }

    @Override
    public boolean isRightClicking() {

        // This already means that player is right clicking.
        // This is also more accurate way to check right clicking
        if (player.isBlocking()) return true;

        int ping = CompatibilityAPI.getCompatibility().getPing(player);
        if (ping > 215) {
            // Ping was more than 215 so lets take player's ping in account
            // when checking if it is still right clicking
            return NumberUtils.hasMillisPassed(lastRightClick, ping + 15);
        }
        return NumberUtils.hasMillisPassed(lastRightClick, 215);
    }

    @Override
    public boolean isSneaking() {
        return player.isSneaking();
    }
    
    @Override
    public boolean isSprinting() {
        return player.isSprinting();
    }

    @Nullable
    @Override
    public ColorType getThermalColor(@Nullable Player player) {
        return thermalScopeData.get(player);
    }
}
