package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.file.inline.Argument;
import me.deecaad.core.file.inline.ArgumentMap;
import me.deecaad.core.file.inline.types.*;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanic;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

public class PotionMechanic extends Mechanic {

    public static final Argument TYPE = new Argument("type", new PotionType());
    public static final Argument TIME = new Argument("time", new IntegerType(0), 100);
    public static final Argument AMPLIFIER = new Argument("level", new IntegerType(1), 1);
    public static final Argument EXTRA_PARTICLES = new Argument("extraParticles", new BooleanType(), false);
    public static final Argument HIDE_PARTICLES = new Argument("hideParticles", new BooleanType(), false);
    public static final Argument HIDE_ICON = new Argument("hideIcon", new BooleanType(), false);

    private final PotionEffect potion;

    public PotionMechanic(Map<Argument, Object> args) {
        super(args);

        PotionEffectType type = (PotionEffectType) args.get(TYPE);
        int time = (int) args.get(TIME);
        int amplifier = (int) args.get(AMPLIFIER) - 1; // subtract 1, since 0 is actually level 1
        boolean extraParticles = (boolean) args.get(EXTRA_PARTICLES);
        boolean hideParticles = (boolean) args.get(HIDE_PARTICLES);
        boolean hideIcon = (boolean) args.get(HIDE_ICON);

        // Icon option was added in Minecraft 1.14
        if (ReflectionUtil.getMCVersion() > 13)
            potion = new PotionEffect(type, time, amplifier, extraParticles, hideParticles, hideIcon);
        else
            potion = new PotionEffect(type, time, amplifier, extraParticles, hideParticles);
    }

    @Override
    public ArgumentMap args() {
        return super.args().addAll(TYPE, TIME, AMPLIFIER, EXTRA_PARTICLES, HIDE_PARTICLES, HIDE_ICON);
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
}