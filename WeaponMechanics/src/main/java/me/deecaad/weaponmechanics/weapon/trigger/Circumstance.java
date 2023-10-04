package me.deecaad.weaponmechanics.weapon.trigger;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerEnumException;
import me.deecaad.core.file.SerializerException;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Circumstance implements Serializer<Circumstance> {

    private List<CircumstanceData> circumstances;

    /**
     * Default constructor for serializer
     */
    public Circumstance() {
    }

    public Circumstance(List<CircumstanceData> circumstances) {
        this.circumstances = circumstances;
    }

    /**
     * If entity wrapper is null, this will always return true
     *
     * @param entityWrapper the entity wrapper or null if not used
     * @return whether to deny
     */
    public boolean deny(@Nullable EntityWrapper entityWrapper) {
        if (entityWrapper == null) return true;

        for (CircumstanceData circumstance : this.circumstances) {
            if (circumstance.deny(entityWrapper)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public Circumstance serialize(@NotNull SerializeData data) throws SerializerException {
        ConfigurationSection circumstanceSection = data.of().assertExists().assertType(ConfigurationSection.class).get();
        List<CircumstanceData> circumstances = new ArrayList<>(1);

        for (String type : circumstanceSection.getKeys(false)) {
            String typeToUpper = type.toUpperCase(Locale.ROOT);

            String value = data.config.getString(data.key + "." + type);
            if (!value.equalsIgnoreCase("DENY") && !value.equalsIgnoreCase("REQUIRED")) {
                throw data.exception(type, "Only DENY and REQUIRED are allowed, now there was " + value + "!");
            }

            try {
                circumstances.add(new CircumstanceData(CircumstanceType.valueOf(typeToUpper), value.equalsIgnoreCase("REQUIRED")));
            } catch (IllegalArgumentException e) {
                throw new SerializerEnumException(this, CircumstanceType.class, type, false, data.of().getLocation());
            }
        }

        return new Circumstance(circumstances);
    }

    private record CircumstanceData(CircumstanceType circumstanceType, boolean required) {
        public boolean deny(EntityWrapper entityWrapper) {
                return required != switch (circumstanceType) {
                    case RELOADING -> entityWrapper.isReloading();
                    case ZOOMING -> entityWrapper.isZooming();
                    case SNEAKING -> entityWrapper.isSneaking();
                    case STANDING -> entityWrapper.isStanding();
                    case WALKING -> entityWrapper.isWalking();
                    case RIDING -> entityWrapper.isRiding();
                    case SPRINTING -> entityWrapper.isSprinting();
                    case DUAL_WIELDING -> entityWrapper.isDualWielding();
                    case SWIMMING -> entityWrapper.isSwimming();
                    case IN_MIDAIR -> entityWrapper.isInMidair();
                    case GLIDING -> entityWrapper.isGliding();
                };
            }
        }

    private enum CircumstanceType {
        RELOADING,
        ZOOMING,
        SNEAKING,
        STANDING,
        WALKING,
        RIDING,
        SPRINTING,
        DUAL_WIELDING,
        SWIMMING,
        IN_MIDAIR,
        GLIDING
    }
}