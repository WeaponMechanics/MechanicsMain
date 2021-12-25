package me.deecaad.weaponmechanics.compatibility.scope;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.scope.ScopeLevel;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Scope_1_8_R3 implements IScopeCompatibility {

    private static final MobEffect NIGHT_VISION = new MobEffect(PotionEffectType.NIGHT_VISION.getId(), 6000, 2);
    private static final Field attributesField;
    private static final Field effectsField;

    static {
        Class<?> attributesPacket = ReflectionUtil.getPacketClass("PacketPlayOutUpdateAttributes");
        Class<?> effectsPacket = ReflectionUtil.getPacketClass("PacketPlayOutRemoveEntityEffect");

        attributesField = ReflectionUtil.getField(attributesPacket, "b");
        effectsField = ReflectionUtil.getField(effectsPacket, "b");

        if (ReflectionUtil.getMCVersion() != 8) {
            WeaponMechanics.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Scope_1_8_R3.class + " when not using Minecraft 8",
                    new InternalError()
            );
        }
    }

    @Override
    public void updateAbilities(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        entityPlayer.playerConnection.sendPacket(new PacketPlayOutAbilities(entityPlayer.abilities));
    }

    @Override
    public void updateAttributesFor(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        List<AttributeInstance> list = new ArrayList<>();
        list.add(entityPlayer.getAttributeMap().a("generic.movementSpeed"));

        // Negative entity id for identifying packet
        entityPlayer.playerConnection.sendPacket(new PacketPlayOutUpdateAttributes(entityPlayer.getId(), list));
    }

    @Override
    public void modifyUpdateAttributesPacket(me.deecaad.core.packetlistener.Packet packet, int zoomAmount) {

        //noinspection unchecked
        List<PacketPlayOutUpdateAttributes.AttributeSnapshot> attributeSnapshots = (List<PacketPlayOutUpdateAttributes.AttributeSnapshot>) packet.getFieldValue(attributesField);

        if (attributeSnapshots.size() > 1) {
            // Don't let external things such as sprint modify movement speed
            attributeSnapshots.removeIf(next -> next.a().equals("generic.movement_speed"));
            return;
        }

        // Don't modify other attributes than movement speed
        if (!attributeSnapshots.get(0).a().equals("generic.movement_speed")) return;

        PacketPlayOutUpdateAttributes.AttributeSnapshot attributeSnapshot = attributeSnapshots.get(0);

        List<AttributeModifier> list = new ArrayList<>();
        list.add(new AttributeModifier(UUID.randomUUID(), "WM_SCOPE", ScopeLevel.getScope(zoomAmount), 0));
        attributeSnapshots.add(new PacketPlayOutUpdateAttributes().new AttributeSnapshot(attributeSnapshot.a(), attributeSnapshot.b(), list));
    }

    @Override
    public void addNightVision(Player player) {
        // 6000 = 5min
        PacketPlayOutEntityEffect entityEffect = new PacketPlayOutEntityEffect(-player.getEntityId(), NIGHT_VISION);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(entityEffect);
    }

    @Override
    public void removeNightVision(Player player) {
        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {

            EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

            // Simply remove the entity effect
            PacketPlayOutRemoveEntityEffect removeEntityEffect = new PacketPlayOutRemoveEntityEffect(player.getEntityId(), NIGHT_VISION);
            entityPlayer.playerConnection.sendPacket(removeEntityEffect);

            // resend the existing one
            MobEffect mobEffect = entityPlayer.getEffect(MobEffectList.byId[PotionEffectType.NIGHT_VISION.getId()]);
            PacketPlayOutEntityEffect entityEffect = new PacketPlayOutEntityEffect(player.getEntityId(), mobEffect);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(entityEffect);
            return;
        }

        // Simply remove the entity effect
        PacketPlayOutRemoveEntityEffect removeEntityEffect = new PacketPlayOutRemoveEntityEffect(player.getEntityId(), NIGHT_VISION);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(removeEntityEffect);
    }

    @Override
    public boolean isRemoveNightVisionPacket(me.deecaad.core.packetlistener.Packet packet) {
        return (int) packet.getFieldValue(effectsField) == NIGHT_VISION.getEffectId();
    }
}
