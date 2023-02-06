package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.SerializerOptionsException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class PotionMechanic extends Mechanic {

    private PotionEffect potion;

    /**
     * Default constructor for serializer.
     */
    public PotionMechanic() {
    }

    public PotionMechanic(PotionEffect potion) {
        this.potion = potion;
    }

    public PotionEffect getPotion() {
        return potion;
    }

    @Override
    public void use0(CastData cast) {
        if (cast.getTarget() == null)
            return;

        cast.getTarget().addPotionEffect(potion);
    }

    @Override
    public String getKeyword() {
        return "Potion";
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://github.com/WeaponMechanics/MechanicsMain/wiki/PotionMechanic";
    }

    @NotNull
    @Override
    public Mechanic serialize(SerializeData data) throws SerializerException {
        String potionStr = data.of("Potion").assertExists().assertType(String.class).get().toString().trim();
        PotionEffectType potion = PotionEffectType.getByName(potionStr.toUpperCase(Locale.ROOT));
        int time = data.of("Time").assertPositive().getInt(100);
        int amplifier = data.of("Level").assertPositive().getInt(1) - 1;
        boolean ambient = data.of("Particles").getEnum(ParticleMode.class, ParticleMode.NORMAL) == ParticleMode.AMBIENT;
        boolean showParticles = data.of("Particles").getEnum(ParticleMode.class, ParticleMode.NORMAL) != ParticleMode.HIDE;
        boolean showIcon = !data.of("Hide_Icon").getBool(false);

        // If we failed to find the potion, try to use the more user-friendly
        // minecraft keys instead of the legacy enum. This also technically
        // supports custom potion effects, but I don't know if those exist...
        if (potion == null && ReflectionUtil.getMCVersion() >= 13) {
            potion = PotionEffectType.getByKey(NamespacedKey.fromString(potionStr.toLowerCase(Locale.ROOT)));
        }

        // Make sure that the potion type exists
        if (potion == null) {
            List<String> options = Arrays.stream(PotionEffectType.values())
                    .flatMap(type -> ReflectionUtil.getMCVersion() >= 13 ? Stream.of(type.getName(), type.getKey().getKey()) : Stream.of(type.getName()))
                    .toList();

            throw new SerializerOptionsException(this, "Potion", options, potionStr, data.of("Potion").getLocation());
        }

        PotionEffect effect = new PotionEffect(potion, time, amplifier, ambient, showParticles);
        if (ReflectionUtil.getMCVersion() > 13)
            effect = new PotionEffect(potion, time, amplifier, ambient, showParticles, showIcon);

        return applyParentArgs(data, new PotionMechanic(effect));
    }

    /**
     * This enum makes it easier for users to select ambient/hide/show instead
     * of using 2 separate booleans.
     */
    public enum ParticleMode {
        HIDE, NORMAL, AMBIENT
    }
}