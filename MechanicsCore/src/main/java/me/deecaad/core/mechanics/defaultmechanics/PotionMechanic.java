package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.utils.ReflectionUtil;
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
        String potionStr = data.of("Potion").assertExists().assertType(String.class).get();
        PotionEffectType potion = PotionEffectType.getByName(potionStr.trim());
        int time = data.of("Time").assertPositive().getInt(100);
        int amplifier = data.of("Level").assertPositive().getInt() - 1;
        boolean ambient = data.of("Particles").getEnum(ParticleMode.class, ParticleMode.NORMAL) == ParticleMode.AMBIENT;
        boolean showParticles = data.of("Particles").getEnum(ParticleMode.class, ParticleMode.NORMAL) != ParticleMode.HIDE;
        boolean showIcon = data.of("Icon").getBool(true);

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