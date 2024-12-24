package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.simple.RegistryValueSerializer;
import me.deecaad.core.mechanics.CastData;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @NotNull @Override
    public Mechanic serialize(@NotNull SerializeData data) throws SerializerException {
        RegistryValueSerializer<PotionEffectType> potionSerializer = new RegistryValueSerializer<>(Registry.EFFECT, true);
        PotionEffectType potion = data.of("Potion").assertExists().serialize(potionSerializer).get().getFirst();
        int time = data.of("Time").assertRange(0, null).getInt().orElse(100);
        int amplifier = data.of("Level").assertRange(0, null).getInt().orElse(1) - 1;
        ParticleMode particleMode = data.of("Particles").getEnum(ParticleMode.class).orElse(ParticleMode.NORMAL);
        boolean ambient = particleMode == ParticleMode.AMBIENT;
        boolean showParticles = particleMode != ParticleMode.HIDE;
        boolean showIcon = !data.of("Hide_Icon").getBool().orElse(false);

        PotionEffect effect = new PotionEffect(potion, time, amplifier, ambient, showParticles, showIcon);
        return applyParentArgs(data, new PotionMechanic(effect));
    }

    /**
     * This enum makes it easier for users to select ambient/hide/show instead of using 2 separate
     * booleans.
     */
    public enum ParticleMode {
        HIDE,
        NORMAL,
        AMBIENT
    }
}