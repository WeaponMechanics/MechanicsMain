package me.deecaad.weaponmechanics.weapon.damage;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.program.MechanicSerializer;

import java.util.Collections;
import java.util.List;

/**
 * Compiles each {@code Damage} sub-section's {@code Mechanics} list explicitly, declaring the
 * per-victim {@code @Victim}/{@code $damage} vocabulary at the compile site.
 */
public class DamageMechanicsValidator implements IValidator {

    /** The {@code Damage} sub-sections, relative to the {@code Damage} section root. */
    private static final List<String> DAMAGE_SUBS = List.of(
        "Mechanics",
        "Kill.Mechanics",
        "Backstab.Mechanics",
        "Critical_Hit.Mechanics",
        "Head.Mechanics",
        "Body.Mechanics",
        "Arms.Mechanics",
        "Legs.Mechanics",
        "Feet.Mechanics");

    @Override
    public String getKeyword() {
        return "Damage";
    }

    @Override
    public List<String> getAllowedPaths() {
        return Collections.singletonList(".Damage");
    }

    @Override
    public void validate(Configuration configuration, SerializeData data) throws SerializerException {
        MechanicSerializer damageMechanics = MechanicSerializer.builder()
            .context("Victim")
            .variables("damage", "armor_damage", "fire_ticks", "distance",
                "is_backstab", "is_critical", "damage_point", "exposure")
            .build();

        for (String sub : DAMAGE_SUBS) {
            data.of(sub).serialize(damageMechanics)
                .ifPresent(mechanics -> configuration.set(data.getKey() + "." + sub, mechanics));
        }
    }
}
