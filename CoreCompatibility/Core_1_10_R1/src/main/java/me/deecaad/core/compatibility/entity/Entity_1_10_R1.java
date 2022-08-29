package me.deecaad.core.compatibility.entity;

import me.deecaad.core.compatibility.equipevent.TriIntConsumer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import net.minecraft.server.v1_10_R1.DataWatcher;
import net.minecraft.server.v1_10_R1.PacketPlayOutEntityMetadata;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_10_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
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

    private static final Field ID = ReflectionUtil.getField(PacketPlayOutEntityMetadata.class, int.class);
    private static final Field DATA = ReflectionUtil.getField(PacketPlayOutEntityMetadata.class, List.class);

    @Override
    public int getId(Object obj) {
        return (int) ReflectionUtil.invokeField(ID, obj);
    }

    @Override
    public Object generateMetaPacket(Entity bukkit) {
        net.minecraft.server.v1_10_R1.Entity entity = ((CraftEntity) bukkit).getHandle();
        return new PacketPlayOutEntityMetadata(entity.getId(), entity.getDataWatcher(), true);
    }

    @Override
    public void modifyMetaPacket(Object obj, EntityMeta meta, boolean enabled) {
        PacketPlayOutEntityMetadata packet = (PacketPlayOutEntityMetadata) obj;
        List<DataWatcher.Item<?>> list = (List<DataWatcher.Item<?>>) ReflectionUtil.invokeField(DATA, packet);

        if (list == null || list.isEmpty())
            return;

        // The "shared byte data" is applied to every entity, and it is always
        // the first item (It can never be the second, third, etc.). However,
        // if no modifications are made to the "shared byte data" before this
        // packet is sent, that item will not be present. This is implemented
        // in vanilla's dirty meta system.
        if (list.get(0) == null || list.get(0).b().getClass() != Byte.class)
            return;

        // noinspection unchecked
        DataWatcher.Item<Byte> item = (DataWatcher.Item<Byte>) list.get(0);
        byte data = item.b();
        data = meta.set(data, enabled);
        item.a(data);
    }
}
