package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.file.inline.Argument;
import me.deecaad.core.file.inline.ArgumentMap;
import me.deecaad.core.file.inline.types.DoubleType;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanic;
import org.bukkit.util.Vector;

import java.util.Map;

public class LeapMechanic extends Mechanic {

    // negative numbers are allowed for backwards
    public static final Argument SPEED = new Argument("speed", new DoubleType());
    public static final Argument VERTICAL_MULTIPLIER = new Argument("verticalMultiplier", new DoubleType(), 1.0);

    private double speed;
    private double verticalMultiplier;

    /**
     * Default constructor for serializer.
     */
    public LeapMechanic() {
    }

    public LeapMechanic(Map<Argument, Object> args) {
        super(args);

        speed = (double) args.get(SPEED);
        verticalMultiplier = (double) args.get(VERTICAL_MULTIPLIER);
    }

    @Override
    public ArgumentMap args() {
        return super.args().addAll(SPEED, VERTICAL_MULTIPLIER);
    }

    @Override
    protected void use0(CastData cast) {
        Vector velocity = cast.getTargetLocation().subtract(cast.getSourceLocation()).toVector();
        velocity.setY(velocity.getY() * verticalMultiplier);
        velocity.normalize().multiply(speed);
        cast.getSource().setVelocity(velocity);
    }

    @Override
    public String getKeyword() {
        return "Leap";
    }
}
