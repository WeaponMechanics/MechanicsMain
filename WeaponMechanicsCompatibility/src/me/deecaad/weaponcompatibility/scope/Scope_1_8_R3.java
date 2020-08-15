package me.deecaad.weaponcompatibility.scope;

import me.deecaad.weaponmechanics.weapon.scope.ScopeLevel;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class Scope_1_8_R3 implements IScopeCompatibility {

    private static final MobEffect NIGHT_VISION = new MobEffect(PotionEffectType.NIGHT_VISION.getId(), 6000, 2);

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
    public void modifyUpdateAttributesPacket(me.deecaad.core.packetlistener.Packet packet, int zoomAmount) {

        //noinspection unchecked
        List<PacketPlayOutUpdateAttributes.AttributeSnapshot> attributeSnapshots = (List<PacketPlayOutUpdateAttributes.AttributeSnapshot>) packet.getFieldValue("b");

        for (PacketPlayOutUpdateAttributes.AttributeSnapshot attributeSnapshot : attributeSnapshots) {
            if (!attributeSnapshot.a().equals("generic.movementSpeed")) continue;

            // I changed AttributeModifier.Operation.ADDITION, hoping for a lucky guess
            // !!! This is probably incorrect !!!
            Collection<AttributeModifier> attributeModifiers = attributeSnapshot.c();
            attributeModifiers.add(new AttributeModifier(UUID.randomUUID(), "WM_SCOPE", ScopeLevel.getScope(zoomAmount), 0));
            break;
        }
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
        return (int) packet.getFieldValue("b") == NIGHT_VISION.getEffectId();
    }
}
