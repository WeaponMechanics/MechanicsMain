package me.deecaad.weaponmechanics.weapon.trigger;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.scope.CastScope;
import me.deecaad.core.mechanics.program.Program;
import me.deecaad.core.mechanics.program.MechanicSerializer;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Circumstance implements Serializer<Circumstance> {

    private List<CircumstanceData> circumstances;
    private Program denyMechanics;

    /**
     * Default constructor for serializer
     */
    public Circumstance() {
    }

    public Circumstance(List<CircumstanceData> circumstances, Program denyMechanics) {
        this.circumstances = circumstances;
        this.denyMechanics = denyMechanics;
    }

    /**
     * If entity wrapper is null, this will always return true
     *
     * @param entityWrapper the entity wrapper or null if not used
     * @return whether to deny
     */
    public boolean deny(@Nullable EntityWrapper entityWrapper) {
        if (entityWrapper == null)
            return true;

        for (CircumstanceData circumstance : this.circumstances) {
            if (circumstance.deny(entityWrapper)) {
                if (denyMechanics != null) {
                    CastScope cast = CastScope.builder(entityWrapper.getEntity()).itemTitle(null).item(null).build();
                    cast.placeholders().put("deny_reason", circumstance.circumstanceType.getHumanName());
                    denyMechanics.run(cast);
                }
                return true;
            }
        }
        return false;
    }

    @NotNull @Override
    public Circumstance serialize(@NotNull SerializeData data) throws SerializerException {
        data.of().assertExists();
        List<CircumstanceData> circumstances = new ArrayList<>(1);
        Program denyMechanics = data.of("Deny_Mechanics").serialize(MechanicSerializer.class).orElse(null);

        for (String type : data.getConfig().getKeys(data.getKey(), false)) {
            if (type.equals("Deny_Mechanics"))
                continue;

            String typeToUpper = type.toUpperCase(Locale.ROOT);

            // safe to get the optional without checking... we are in the loop of existing keys :)
            String value = data.of(type).get(String.class).get();
            if (!value.equalsIgnoreCase("DENY") && !value.equalsIgnoreCase("REQUIRED")) {
                throw data.exception(type, "Only DENY and REQUIRED are allowed, now there was " + value + "!");
            }

            try {
                circumstances.add(new CircumstanceData(CircumstanceType.valueOf(typeToUpper), value.equalsIgnoreCase("REQUIRED")));
            } catch (IllegalArgumentException e) {
                throw SerializerException.builder()
                    .located(data.of(type).errorLocation())
                    .buildInvalidEnumOption(type, CircumstanceType.class);
            }
        }

        return new Circumstance(circumstances, denyMechanics);
    }

    public record CircumstanceData(CircumstanceType circumstanceType, boolean required) {
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
                case AMMO_EMPTY -> entityWrapper.isAmmoEmpty();
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
        GLIDING,
        AMMO_EMPTY;

        private final String humanName;

        CircumstanceType() {
            this.humanName = name().toLowerCase(Locale.ROOT).replace("_", " ");
        }

        public String getHumanName() {
            return humanName;
        }
    }
}