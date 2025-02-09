package me.deecaad.weaponmechanics.weapon.firearm;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.utils.CustomTag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FirearmAction implements Serializer<FirearmAction> {

    private FirearmType firearmType;
    private int firearmActionFrequency;
    private int openTime;
    private int closeTime;
    private Mechanics open;
    private Mechanics close;

    /**
     * Default constructor for serializer
     */
    public FirearmAction() {
    }

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
            if (open != null)
                open.use(castData);
            return;
        }
        if (close != null)
            close.use(castData);
    }

    public FirearmState getState(ItemStack weaponStack) {
        int state = CustomTag.FIREARM_ACTION_STATE.getInteger(weaponStack);
        return switch (state) {
            case 1 -> FirearmState.OPEN;
            case 2 -> FirearmState.CLOSE;
            default -> FirearmState.READY;
        };
    }

    public void changeState(ItemStack weaponStack, FirearmState state) {
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

    public Mechanics getOpen() {
        return open;
    }

    public Mechanics getClose() {
        return close;
    }

    public int getFirearmActionFrequency() {
        return firearmActionFrequency;
    }

    @Override
    public String getKeyword() {
        return "Firearm_Action";
    }

    @Override
    public boolean shouldSerialize(@NotNull SerializeData data) {
        // only serialize if 1 under root
        String key = data.getKey();
        if (key == null)
            return false;

        int depth = StringUtil.countOccurrences(data.getKey(), '.');
        return depth == 1;
    }

    @Override
    @NotNull public FirearmAction serialize(@NotNull SerializeData data) throws SerializerException {

        FirearmType type = data.of("Type").assertExists().getEnum(FirearmType.class).get();

        // Default to 1 in order to make it easier to use
        int firearmActionFrequency = data.of("Firearm_Action_Frequency").assertRange(1, null).getInt().orElse(1);
        int openTime = data.of("Open.Time").assertRange(1, null).getInt().orElse(1);
        int closeTime = data.of("Close.Time").assertRange(1, null).getInt().orElse(1);

        Mechanics open = data.of("Open.Mechanics").serialize(Mechanics.class).orElse(null);
        Mechanics close = data.of("Close.Mechanics").serialize(Mechanics.class).orElse(null);

        return new FirearmAction(type, firearmActionFrequency, openTime, closeTime, open, close);
    }
}