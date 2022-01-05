package me.deecaad.weaponmechanics.compatibility.scope;

import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.scope.ScopeLevel;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_18_R1.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Scope_1_18_R1 implements IScopeCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 18) {
            WeaponMechanics.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Scope_1_18_R1.class + " when not using Minecraft 18",
                    new InternalError()
            );
        }
    }

    @Override
    public void updateAbilities(org.bukkit.entity.Player player) {
        ServerPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        entityPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(entityPlayer.getAbilities()));
    }

    @Override
    public void updateAttributesFor(org.bukkit.entity.Player player) {
        ServerPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        List<AttributeInstance> list = new ArrayList<>();
        list.add(entityPlayer.getAttributes().getInstance(CraftAttributeMap.toMinecraft(Attribute.GENERIC_MOVEMENT_SPEED)));

        // Negative entity id for identifying packet
        entityPlayer.connection.send(new ClientboundUpdateAttributesPacket(entityPlayer.getId(), list));
    }

    @Override
    public void modifyUpdateAttributesPacket(me.deecaad.core.packetlistener.Packet packet, int zoomAmount) {

        List<ClientboundUpdateAttributesPacket.AttributeSnapshot> attributeSnapshots = ((ClientboundUpdateAttributesPacket) packet.getPacket()).getValues();

        if (attributeSnapshots.size() > 1) {
            // Don't let external things such as sprint modify movement speed
            attributeSnapshots.removeIf(next -> next.getAttribute() == CraftAttributeMap.toMinecraft(Attribute.GENERIC_MOVEMENT_SPEED));
            return;
        }

        // Don't modify other attributes than movement speed
        if (attributeSnapshots.get(0).getAttribute() != CraftAttributeMap.toMinecraft(Attribute.GENERIC_MOVEMENT_SPEED)) return;

        ClientboundUpdateAttributesPacket.AttributeSnapshot attributeSnapshot = attributeSnapshots.get(0);

        List<AttributeModifier> list = new ArrayList<>();
        list.add(new AttributeModifier(UUID.randomUUID(), () -> "WM_SCOPE", ScopeLevel.getScope(zoomAmount), AttributeModifier.Operation.ADDITION));
        attributeSnapshots.add(new ClientboundUpdateAttributesPacket.AttributeSnapshot(attributeSnapshot.getAttribute(), attributeSnapshot.getBase(), list));
    }

    @Override
    public void addNightVision(org.bukkit.entity.Player player) {
        // 6000 = 5min
        ClientboundUpdateMobEffectPacket entityEffect = new ClientboundUpdateMobEffectPacket(-player.getEntityId(), new MobEffectInstance(MobEffect.byId(PotionEffectType.NIGHT_VISION.getId()), 6000, 2));
        ((CraftPlayer) player).getHandle().connection.send(entityEffect);
    }

    @Override
    public void removeNightVision(org.bukkit.entity.Player player) {
        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {

            ServerPlayer entityPlayer = ((CraftPlayer) player).getHandle();

            // Simply remove the entity effect
            ClientboundRemoveMobEffectPacket removeEntityEffect = new ClientboundRemoveMobEffectPacket(player.getEntityId(), MobEffect.byId(PotionEffectType.NIGHT_VISION.getId()));
            entityPlayer.connection.send(removeEntityEffect);

            // resend the existing one
            MobEffectInstance mobEffect = entityPlayer.getEffect(MobEffect.byId(PotionEffectType.NIGHT_VISION.getId()));
            ClientboundUpdateMobEffectPacket entityEffect = new ClientboundUpdateMobEffectPacket(player.getEntityId(), mobEffect);
            ((CraftPlayer) player).getHandle().connection.send(entityEffect);
            return;
        }

        // Simply remove the entity effect
        ClientboundRemoveMobEffectPacket removeEntityEffect = new ClientboundRemoveMobEffectPacket(player.getEntityId(), MobEffect.byId(PotionEffectType.NIGHT_VISION.getId()));
        ((CraftPlayer) player).getHandle().connection.send(removeEntityEffect);
    }

    @Override
    public boolean isRemoveNightVisionPacket(me.deecaad.core.packetlistener.Packet packet) {
        // 16 = night vision
        return ((ClientboundRemoveMobEffectPacket) packet.getPacket()).getEffect() == MobEffect.byId(16);
    }
}