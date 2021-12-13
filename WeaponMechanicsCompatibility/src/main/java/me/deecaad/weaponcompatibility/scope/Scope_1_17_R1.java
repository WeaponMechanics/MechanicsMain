package me.deecaad.weaponcompatibility.scope;

import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.weapon.scope.ScopeLevel;
import net.minecraft.network.protocol.game.PacketPlayOutAbilities;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEffect;
import net.minecraft.network.protocol.game.PacketPlayOutRemoveEntityEffect;
import net.minecraft.network.protocol.game.PacketPlayOutUpdateAttributes;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.entity.ai.attributes.AttributeModifiable;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerAbilities;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_17_R1.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class Scope_1_17_R1 implements IScopeCompatibility {

    @Override
    public void updateAbilities(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        entityPlayer.b.sendPacket(new PacketPlayOutAbilities((PlayerAbilities) ReflectionUtil.invokeField(ReflectionUtil.getField(EntityHuman.class, "cq"), entityPlayer)));
    }

    @Override
    public void updateAttributesFor(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        List<AttributeModifiable> list = new ArrayList<>();
        list.add(entityPlayer.getAttributeMap().a(CraftAttributeMap.toMinecraft(Attribute.GENERIC_MOVEMENT_SPEED)));

        // Negative entity id for identifying packet
        entityPlayer.b.sendPacket(new PacketPlayOutUpdateAttributes(entityPlayer.getId(), list));
    }

    @Override
    public void modifyUpdateAttributesPacket(me.deecaad.core.packetlistener.Packet packet, int zoomAmount) {

        List<PacketPlayOutUpdateAttributes.AttributeSnapshot> attributeSnapshots = ((PacketPlayOutUpdateAttributes) packet.getPacket()).c();

        if (attributeSnapshots.size() > 1) {
            // Don't let external things such as sprint modify movement speed
            attributeSnapshots.removeIf(next -> next.a() == CraftAttributeMap.toMinecraft(Attribute.GENERIC_MOVEMENT_SPEED));
            return;
        }

        // Don't modify other attributes than movement speed
        if (attributeSnapshots.get(0).a() != CraftAttributeMap.toMinecraft(Attribute.GENERIC_MOVEMENT_SPEED)) return;

        PacketPlayOutUpdateAttributes.AttributeSnapshot attributeSnapshot = attributeSnapshots.get(0);

        List<AttributeModifier> list = new ArrayList<>();
        list.add(new AttributeModifier(UUID.randomUUID(), () -> "WM_SCOPE", ScopeLevel.getScope(zoomAmount), AttributeModifier.Operation.a));
        attributeSnapshots.add(new PacketPlayOutUpdateAttributes.AttributeSnapshot(attributeSnapshot.a(), attributeSnapshot.b(), list));
    }

    @Override
    public void addNightVision(Player player) {
        // 6000 = 5min
        PacketPlayOutEntityEffect entityEffect = new PacketPlayOutEntityEffect(-player.getEntityId(), new MobEffect(MobEffectList.fromId(PotionEffectType.NIGHT_VISION.getId()), 6000, 2));
        ((CraftPlayer) player).getHandle().b.sendPacket(entityEffect);
    }

    @Override
    public void removeNightVision(Player player) {
        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {

            EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

            // Simply remove the entity effect
            PacketPlayOutRemoveEntityEffect removeEntityEffect = new PacketPlayOutRemoveEntityEffect(player.getEntityId(), MobEffectList.fromId(PotionEffectType.NIGHT_VISION.getId()));
            entityPlayer.b.sendPacket(removeEntityEffect);

            // resend the existing one
            MobEffect mobEffect = entityPlayer.getEffect(MobEffectList.fromId(PotionEffectType.NIGHT_VISION.getId()));
            PacketPlayOutEntityEffect entityEffect = new PacketPlayOutEntityEffect(player.getEntityId(), mobEffect);
            ((CraftPlayer) player).getHandle().b.sendPacket(entityEffect);
            return;
        }

        // Simply remove the entity effect
        PacketPlayOutRemoveEntityEffect removeEntityEffect = new PacketPlayOutRemoveEntityEffect(player.getEntityId(), MobEffectList.fromId(PotionEffectType.NIGHT_VISION.getId()));
        ((CraftPlayer) player).getHandle().b.sendPacket(removeEntityEffect);
    }

    @Override
    public boolean isRemoveNightVisionPacket(me.deecaad.core.packetlistener.Packet packet) {
        // 16 = night vision
        return ((PacketPlayOutRemoveEntityEffect) packet.getPacket()).b() == MobEffectList.fromId(16);
    }
}