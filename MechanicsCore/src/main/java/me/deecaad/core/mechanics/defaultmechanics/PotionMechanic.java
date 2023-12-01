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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        return "https://cjcrafter.gitbook.io/mechanics/mechanics/potion";
    }

    @NotNull
    @Override
    public Mechanic serialize(@NotNull SerializeData data) throws SerializerException {
        String potionStr = data.of("Potion").assertExists().assertType(String.class).get().toString().trim();
        String potionLower = potionStr.toLowerCase(Locale.ROOT);
        PotionEffectType potion = PotionEffectType.getByName(potionStr.toUpperCase(Locale.ROOT));
        int time = data.of("Time").assertPositive().getInt(100);
        int amplifier = data.of("Level").assertPositive().getInt(1) - 1;
        boolean ambient = data.of("Particles").getEnum(ParticleMode.class, ParticleMode.NORMAL) == ParticleMode.AMBIENT;
        boolean showParticles = data.of("Particles").getEnum(ParticleMode.class, ParticleMode.NORMAL) != ParticleMode.HIDE;
        boolean showIcon = !data.of("Hide_Icon").getBool(false);

        // If we failed to find the potion, try to use the more user-friendly
        // minecraft keys instead of the legacy enum. This also technically
        // supports custom potion effects, but I don't know if those exist...
        if (potion == null && ReflectionUtil.getMCVersion() >= 18) {
            potion = PotionEffectType.getByKey(NamespacedKey.fromString(potionLower));
        }

        // Try by name for name support
        if (potion == null) {
            potion = PotionEffectType.getByName(potionLower);
        }

        if (potion == null) {
            List<String> options = new ArrayList<>();
            for (PotionEffectType type : PotionEffectType.values()) {
                options.add(type.getName());
                if (ReflectionUtil.getMCVersion() >= 18)
                    options.add(type.getKey().getKey());
            }

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