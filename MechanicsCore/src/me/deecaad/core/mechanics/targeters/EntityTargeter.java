package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.mechanics.serialization.SerializerData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.function.Predicate;

@SerializerData(name = "ENTITY_TARGETER_NO_USE", args = {"isLiving~BOOLEAN~living", "entity~ENTITY~type"})
public abstract class EntityTargeter implements Targeter<Entity> {

    protected Predicate<Entity> predicate = entity -> isAllowed(entity.getType());

    protected boolean living;
    protected EntityType only;

    protected EntityTargeter() {
    }

    public boolean isLiving() {
        return living;
    }

    public void setLiving(boolean living) {
        this.living = living;
    }

    public EntityType getOnly() {
        return only;
    }

    public void setOnly(EntityType only) {
        this.only = only;
    }

    public boolean isAllowed(EntityType type) {
        if (only != null && only == type) {
            return true;
        } else return living && type.isAlive();
    }

    @Override
    public Targeter<Entity> serialize(Map<String, Object> data) {
        boolean isLiving = (boolean) data.getOrDefault("isLiving", false);
        EntityType only = (EntityType) data.get("entity");

        setLiving(isLiving);
        setOnly(only);
        return this;
    }
}
