package me.deecaad.weaponcompatibility.scope;

import me.deecaad.core.packetlistener.Packet;
import me.deecaad.weaponmechanics.weapon.scope.ScopeLevel;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class Scope_1_15_R1 implements IScopeCompatibility {

    @Override
    public void updateAbilities(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        entityPlayer.playerConnection.sendPacket(new PacketPlayOutAbilities(entityPlayer.abilities));
    }

    @Override
    public void updateAttributesFor(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        AttributeMapServer attributemapserver = (AttributeMapServer) entityPlayer.getAttributeMap();
        Collection<AttributeInstance> collection = attributemapserver.c();
        if (!collection.isEmpty()) {
            entityPlayer.playerConnection.sendPacket(new PacketPlayOutUpdateAttributes(entityPlayer.getId(), collection));
        }
    }

    @Override
    public void modifyUpdateAttributesPacket(Packet packet, int zoomAmount) {

        //noinspection unchecked
        List<PacketPlayOutUpdateAttributes.AttributeSnapshot> attributeSnapshots = (List<PacketPlayOutUpdateAttributes.AttributeSnapshot>) packet.getFieldValue("b");

        for (PacketPlayOutUpdateAttributes.AttributeSnapshot attributeSnapshot : attributeSnapshots) {
            if (!attributeSnapshot.a().equals("generic.movementSpeed")) continue;

            Collection<AttributeModifier> attributeModifiers = attributeSnapshot.c();
            attributeModifiers.add(new AttributeModifier(UUID.randomUUID(), () -> "WM_SCOPE", ScopeLevel.getScope(zoomAmount), AttributeModifier.Operation.ADDITION));
            break;
        }
    }

    @Override
    public void addNightVision(Player player) {
        // 6000 = 5min
        PacketPlayOutEntityEffect entityEffect = new PacketPlayOutEntityEffect(-player.getEntityId(), new MobEffect(MobEffectList.fromId(PotionEffectType.NIGHT_VISION.getId()), 6000, 2));
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(entityEffect);
    }

    @Override
    public void removeNightVision(Player player) {
        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {

            EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

            // Simply remove the entity effect
            PacketPlayOutRemoveEntityEffect removeEntityEffect = new PacketPlayOutRemoveEntityEffect(player.getEntityId(), MobEffectList.fromId(PotionEffectType.NIGHT_VISION.getId()));
            entityPlayer.playerConnection.sendPacket(removeEntityEffect);

            // resend the existing one
            MobEffect mobEffect = entityPlayer.getEffect(MobEffectList.fromId(PotionEffectType.NIGHT_VISION.getId()));
            PacketPlayOutEntityEffect entityEffect = new PacketPlayOutEntityEffect(player.getEntityId(), mobEffect);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(entityEffect);
            return;
        }

        // Simply remove the entity effect
        PacketPlayOutRemoveEntityEffect removeEntityEffect = new PacketPlayOutRemoveEntityEffect(player.getEntityId(), MobEffectList.fromId(PotionEffectType.NIGHT_VISION.getId()));
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(removeEntityEffect);
    }

    @Override
    public boolean isRemoveNightVisionPacket(Packet packet) {
        return packet.getFieldValue("b") == MobEffects.NIGHT_VISION;
    }
}