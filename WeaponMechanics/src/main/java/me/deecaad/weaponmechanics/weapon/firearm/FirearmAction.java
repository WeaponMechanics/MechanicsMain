package me.deecaad.weaponmechanics.weapon.firearm;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.io.File;

import static me.deecaad.core.MechanicsCore.debug;

public class FirearmAction implements Serializer<FirearmAction> {

    private FirearmType firearmType;
    private int firearmActionFrequency;
    private int openTime;
    private int closeTime;
    private Mechanics open;
    private Mechanics close;

    /**
     * Empty constructor to be used as serializer
     */
    public FirearmAction() { }

    public FirearmAction(FirearmType firearmType, int firearmActionFrequency, int openTime, int closeTime, Mechanics open, Mechanics close) {
        this.firearmType = firearmType;
        this.firearmActionFrequency = firearmActionFrequency;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.open = open;
        this.close = close;
    }

    public void useMechanics(CastData castData, boolean isOpen) {
        if (isOpen) {
            if (open != null) open.use(castData);
            return;
        }
        if (close != null) close.use(castData);
    }

    /**
     * @return whether the state is READY
     */
    public boolean hasReadyFirearmActions(ItemStack weaponStack) {
        return getState(weaponStack) == FirearmState.READY;
    }

    public boolean hasReloadState(ItemStack weaponStack) {
        FirearmState state = getState(weaponStack);
        return state == FirearmState.RELOAD_OPEN || state == FirearmState.RELOAD || state == FirearmState.RELOAD_CLOSE;
    }

    public boolean hasShootState(ItemStack weaponStack) {
        FirearmState state = getState(weaponStack);
        return state == FirearmState.SHOOT_OPEN || state == FirearmState.SHOOT_CLOSE;
    }

    public FirearmState getState(ItemStack weaponStack) {
        int state = CustomTag.FIREARM_ACTION_STATE.getInteger(weaponStack);
        switch (state) {
            case 1:
                return FirearmState.RELOAD_OPEN;
            case 2:
                return FirearmState.RELOAD;
            case 3:
                return FirearmState.RELOAD_CLOSE;
            case 4:
                return FirearmState.SHOOT_OPEN;
            case 5:
                return FirearmState.SHOOT_CLOSE;
            default:
                return FirearmState.READY;
        }
    }

    public void readyState(ItemStack weaponStack) {
        changeState(weaponStack, FirearmState.READY);
    }

    public void openReloadState(ItemStack weaponStack) {
        changeState(weaponStack, FirearmState.RELOAD_OPEN);
    }

    public void reloadState(ItemStack weaponStack) {
        changeState(weaponStack, FirearmState.RELOAD);
    }

    public void closeReloadState(ItemStack weaponStack) {
        changeState(weaponStack, FirearmState.RELOAD_CLOSE);
    }

    public void openShootState(ItemStack weaponStack) {
        changeState(weaponStack, FirearmState.SHOOT_OPEN);
    }

    public void closeShootState(ItemStack weaponStack) {
        changeState(weaponStack, FirearmState.SHOOT_CLOSE);
    }

    private void changeState(ItemStack weaponStack, FirearmState state) {
        CustomTag.FIREARM_ACTION_STATE.setInteger(weaponStack, state.getId());
    }

    public FirearmType getFirearmType() {
        return firearmType;
    }

    public int getOpenTime() {
        return openTime;
    }

    public int getCloseTime() {
        return closeTime;
    }

    public int getFirearmActionFrequency() {
        return firearmActionFrequency;
    }

    @Override
    public String getKeyword() {
        return "Firearm_Action";
    }

    @Override
    @Nonnull
    public FirearmAction serialize(SerializeData data) throws SerializerException {

        FirearmType type = data.of("Type").assertExists().getEnum(FirearmType.class);

        // Default to 1 in order to make it easier to use
        int firearmActionFrequency = data.of("Firearm_Action_Frequency").assertRange(1, Integer.MAX_VALUE).getInt(1);
        int openTime = data.of("Open.Time").assertRange(1, Integer.MAX_VALUE).getInt(1);
        int closeTime = data.of("Close.Time").assertRange(1, Integer.MAX_VALUE).getInt(1);

        Mechanics open = data.of("Open.Mechanics").serialize(Mechanics.class);
        Mechanics close = data.of("Close.Mechanics").serialize(Mechanics.class);

        return new FirearmAction(type, firearmActionFrequency, openTime, closeTime, open, close);
    }
}