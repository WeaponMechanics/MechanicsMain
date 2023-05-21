package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.core.mechanics.conditions.Condition;
import me.deecaad.core.mechanics.targeters.Targeter;
import me.deecaad.core.mechanics.targeters.WorldTargeter;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class CustomSoundMechanic extends Mechanic {

    private String sound;
    private float volume;
    private float pitch;
    private float noise;
    private Object category; // store as an Object to avoid version mismatch errors in <1.11
    private Targeter listeners;
    private List<Condition> listenerConditions;

    /**
     * Default constructor for serializer.
     */
    public CustomSoundMechanic() {
    }

    public CustomSoundMechanic(String sound, float volume, float pitch, float noise, Object category, Targeter listeners, List<Condition> listenerConditions) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.noise = noise;
        this.category = category;
        this.listeners = listeners;
        this.listenerConditions = listenerConditions;
    }

    public String getSound() {
        return sound;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }

    public float getNoise() {
        return noise;
    }

    public Object getCategory() {
        return category;
    }

    public Targeter getListeners() {
        return listeners;
    }

    public List<Condition> getListenerConditions() {
        return listenerConditions;
    }

    @Override
    protected void use0(CastData cast) {
        if (listeners == null) {
            Location loc = cast.getTargetLocation();

            loc.getWorld().playSound(loc, sound, (SoundCategory) category, volume, pitch + NumberUtil.random(-noise, noise));
            return;
        }

        // Imagine an explosion. It has a target location. So the distance between
        // the source (the player who caused the explosion) and the target (the
        // explosion) will be constant. In reality, for listenerConditions, we
        // want the target to be the listener.
        CastData center = cast;
        if (cast.hasTargetLocation()) {
            center = center.clone();
            center.setTargetLocation((Supplier<Location>) null);
        }

        // When listeners != null, only targeted Players will be able to hear
        // this sound. In this case, we have to loop through every player and
        // manually play the sound packet for them.
        OUTER:
        for (CastData target : listeners.getTargets(center)) {
            if (!(target.getTarget() instanceof Player player))
                continue;

            for (Condition condition : listenerConditions)
                if (!condition.isAllowed(target))
                    continue OUTER;

            player.playSound(cast.getTargetLocation(), sound, (SoundCategory) category, volume, pitch + NumberUtil.random(-noise, noise));
        }
    }

    @Override
    public String getKeyword() {
        return "Custom_Sound";
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://github.com/WeaponMechanics/MechanicsMain/wiki/CustomSoundMechanic";
    }

    @NotNull
    @Override
    public Mechanic serialize(SerializeData data) throws SerializerException {
        String sound = data.of("Sound").assertExists().assertType(String.class).get();
        float volume = (float) data.of("Volume").assertPositive().getDouble(1.0);
        float pitch = (float) data.of("Pitch").assertRange(0.5, 2.0).getDouble(1.0);
        float noise = (float) data.of("Noise").assertRange(0.0, 1.5).getDouble(0.0);
        Object category = ReflectionUtil.getMCVersion() < 11 ? null : data.of("Category").getEnum(SoundCategory.class, SoundCategory.PLAYERS);

        Targeter listeners = data.of("Listeners").getRegistry(Mechanics.TARGETERS, null);
        List<Condition> listenerConditions = data.of("Listener_Conditions").getRegistryList(Mechanics.CONDITIONS);

        // If the user wants to use listener conditions, be sure to use a
        // targeter for listeners (Otherwise these conditions are ignored).
        if (!listenerConditions.isEmpty() && listeners == null)
            listeners = new WorldTargeter();

        return applyParentArgs(data, new CustomSoundMechanic(sound, volume, pitch, noise, category, listeners, listenerConditions));
    }
}