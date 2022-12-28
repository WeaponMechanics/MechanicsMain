package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.inline.Argument;
import me.deecaad.core.file.inline.ArgumentMap;
import me.deecaad.core.file.inline.InlineException;
import me.deecaad.core.file.inline.types.*;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanic;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.core.mechanics.targeters.Targeter;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.Map;

public class CustomSoundMechanic extends Mechanic {

    public static final Argument SOUND = new Argument("sound", new StringType());
    public static final Argument VOLUME = new Argument("volume", new DoubleType(0.0), 1.0);
    public static final Argument PITCH = new Argument("pitch", new DoubleType(0.5, 2.0), 1.0);
    public static final Argument NOISE = new Argument("noise", new DoubleType(0.0, 1.0), 0.0);
    public static final Argument CATEGORY = new Argument("category", new StringType(), null); // only use Enum in 1.11+
    public static final Argument LISTENERS = new Argument("listeners", new RegistryType<>(Mechanics.TARGETERS), null);

    private final String sound;
    private final float volume;
    private final float pitch;
    private final float noise;
    private final Object category; // store as an Object to avoid version mismatch errors in <1.11
    private final Targeter listeners;

    public CustomSoundMechanic(Map<Argument, Object> args) {
        sound = (String) args.get(SOUND);
        volume = ((Number) args.get(VOLUME)).floatValue();
        pitch = ((Number) args.get(PITCH)).floatValue();
        noise = ((Number) args.get(NOISE)).floatValue();
        listeners = (Targeter) args.get(LISTENERS);

        if (ReflectionUtil.getMCVersion() < 11)
            category = null;
        else
            category = (SoundCategory) args.get(CATEGORY);

    }

    @Override
    public ArgumentMap args() {
        ArgumentMap temp = super.args().addAll(SOUND, VOLUME, PITCH, NOISE);

        // SoundCategory enum was added to spigot in 1.11. In older versions,
        // we have a check to make sure that NOBODY tries to use the category
        // argument at all. In newer versions, the argument uses the enum.
        if (ReflectionUtil.getMCVersion() < 11)
            temp.addAll(new Argument("category", new StringType(), null).addValidator(arg -> {
                throw new InlineException("", new SerializerException("", new String[]{"The sound 'category' argument is only for MC 1.11+",
                        "Detected MC version: 1." + ReflectionUtil.getMCVersion() + ".x"}, ""));
            }));
        else
            temp.addAll(new Argument("category", new EnumType<>(SoundCategory.class), SoundCategory.PLAYERS));

        return temp;
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
        for (CastData target : listeners.getTargets(cast)) {
            if (target.getTarget() instanceof Player player) {
                if (ReflectionUtil.getMCVersion() < 11)
                    player.playSound(cast.getTargetLocation(), sound, volume, pitch + NumberUtil.random(-noise, noise));
                else
                    player.playSound(cast.getTargetLocation(), sound, (SoundCategory) category, volume, pitch + NumberUtil.random(-noise, noise));
            }
        }
    }

    @Override
    public String getKeyword() {
        return "Custom_Sound";
    }
}