package me.deecaad.core.mechanics.conditions;

import me.deecaad.core.file.inline.Argument;
import me.deecaad.core.file.inline.ArgumentMap;
import me.deecaad.core.file.inline.types.EnumType;
import me.deecaad.core.mechanics.CastData;
import org.bukkit.entity.EntityType;

import java.util.Map;

public class EntityTypeCondition extends Condition {

    public static final Argument TYPE = new Argument("type", new EnumType<>(EntityType.class));

    private EntityType type;

    /**
     * Default constructor for serializer.
     */
    public EntityTypeCondition() {
    }

    public EntityTypeCondition(Map<Argument, Object> args) {
        this.type = (EntityType) args.get(TYPE);
    }

    @Override
    public ArgumentMap args() {
        return new ArgumentMap(TYPE);
    }

    @Override
    public String getKeyword() {
        return "Entity";
    }

    @Override
    public boolean isAllowed(CastData cast) {
        return cast.getTarget() != null && cast.getTarget().getType() == type;
    }
}
