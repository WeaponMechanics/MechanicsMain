package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanic;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.core.mechanics.conditions.Condition;
import me.deecaad.core.mechanics.targeters.Targeter;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SoundMechanic extends Mechanic {

    private Sound sound;
    private float volume;
    private float pitch;
    private float noise;
    private Object category; // store as an Object to avoid version mismatch errors in <1.11
    private Targeter listeners;
    private List<Condition> listenerConditions;

    /**
     * Default constructor for serializer.
     */
    public SoundMechanic() {
    }

    public SoundMechanic(Sound sound, float volume, float pitch, float noise, Object category, Targeter listeners, List<Condition> listenerConditions) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.noise = noise;
        this.category = category;
        this.listeners = listeners;
        this.listenerConditions = listenerConditions;
    }

    @Override
    protected void use0(CastData cast) {
        if (listeners == null) {
            Location loc = cast.getTargetLocation();

            if (ReflectionUtil.getMCVersion() < 11)
                loc.getWorld().playSound(loc, sound, volume, pitch + NumberUtil.random(-noise, noise));
            else
                loc.getWorld().playSound(loc, sound, (SoundCategory) category, volume, pitch + NumberUtil.random(-noise, noise));
            return;
        }

        // When listeners != null, only targeted Players will be able to hear
        // this sound. In this case, we have to loop through every player and
        // manually play the sound packet for them.
        OUTER:
        for (CastData target : listeners.getTargets(cast)) {
            if (!(target.getTarget() instanceof Player player))
                continue;

            for (Condition condition : listenerConditions)
                if (!condition.isAllowed(target))
                    continue OUTER;

            if (ReflectionUtil.getMCVersion() < 11)
                player.playSound(cast.getTargetLocation(), sound, volume, pitch + NumberUtil.random(-noise, noise));
            else
                player.playSound(cast.getTargetLocation(), sound, (SoundCategory) category, volume, pitch + NumberUtil.random(-noise, noise));
        }
    }

    @Override
    public String getKeyword() {
        return "Custom_Sound";
    }

    @NotNull
    @Override
    public Mechanic serialize(SerializeData data) throws SerializerException {
        Sound sound = data.of("Sound").assertExists().getEnum(Sound.class);
        float volume = (float) data.of("Volume").assertPositive().getDouble(1.0);
        float pitch = (float) data.of("Pitch").assertRange(0.5, 2.0).getDouble(1.0);
        float noise = (float) data.of("Noise").assertRange(0.0, 1.5).getDouble(0.0);
        Object category = ReflectionUtil.getMCVersion() < 11 ? null : data.of("Category").getEnum(SoundCategory.class, SoundCategory.PLAYERS);

        Targeter listeners = data.of("Listeners").getRegistry(Mechanics.TARGETERS);
        List<Condition> listenerConditions = data.of("Listener_Conditions").getRegistryList(Mechanics.CONDITIONS);

        return applyParentArgs(data, new SoundMechanic(sound, volume, pitch, noise, category, listeners, listenerConditions));
    }
}