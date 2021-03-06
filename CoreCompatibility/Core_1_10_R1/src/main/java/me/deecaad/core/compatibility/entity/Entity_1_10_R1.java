package me.deecaad.core.compatibility.entity;

import me.deecaad.core.compatibility.equipevent.TriIntConsumer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_10_R1.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Entity_1_10_R1 implements EntityCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 10) {
            me.deecaad.core.MechanicsCore.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Entity_1_10_R1.class + " when not using Minecraft 10",
                    new InternalError()
            );
        }
    }

    @Override
    public boolean hasCooldown(Player player, Material material) {
        return ((CraftPlayer) player).getHandle().df().a(CraftMagicNumbers.getItem(material));
    }

    @Override
    public void setCooldown(Player player, Material material, int ticks) {
        ((CraftPlayer) player).getHandle().df().a(CraftMagicNumbers.getItem(material), ticks);
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
        return null;
    }

    @Override
    public FakeEntity generateFakeEntity(Location location, EntityType type, Object data) {
        return new FakeEntity_1_10_R1(location, type, data);
    }
}
