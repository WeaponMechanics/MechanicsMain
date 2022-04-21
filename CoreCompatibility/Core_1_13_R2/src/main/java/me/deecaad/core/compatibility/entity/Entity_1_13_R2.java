package me.deecaad.core.compatibility.entity;

import me.deecaad.core.compatibility.equipevent.NonNullList_1_13_R2;
import me.deecaad.core.compatibility.equipevent.TriIntConsumer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Entity_1_13_R2 implements EntityCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 13) {
            me.deecaad.core.MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Entity_1_13_R2.class + " when not using Minecraft 13",
                    new InternalError()
            );
        }
    }

    @Override
    public double getAbsorption(@NotNull LivingEntity entity) {
        return ((CraftLivingEntity) entity).getHandle().getAbsorptionHearts();
    }

    @Override
    public void setAbsorption(@NotNull LivingEntity entity, double absorption) {
        ((CraftLivingEntity) entity).getHandle().setAbsorptionHearts((float) absorption);
    }

    @Override
    public List generateNonNullList(int size, TriIntConsumer<org.bukkit.inventory.ItemStack, org.bukkit.inventory.ItemStack> consumer) {
        return new NonNullList_1_13_R2(size, consumer);
    }

    @Override
    public FakeEntity generateFakeEntity(Location location, EntityType type, Object data) {
        return new FakeEntity_1_13_R2(location, type, data);
    }
}

