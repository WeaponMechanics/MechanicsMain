package me.deecaad.weaponmechanics.compatibility.scope;

import com.comphenix.protocol.events.PacketEvent;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Abilities;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.potion.CraftPotionEffectType;
import org.bukkit.potion.PotionEffectType;

public class Scope_1_21_R3 implements IScopeCompatibility {

    private static final Holder<MobEffect> NIGHT_VISION = Holder.direct(CraftPotionEffectType.bukkitToMinecraft(PotionEffectType.NIGHT_VISION));

    @Override
    public void updateAbilities(org.bukkit.entity.Player player) {
        ServerPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        Abilities abilities = entityPlayer.getAbilities();
        entityPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(abilities));
    }

    @Override
    public void addNightVision(org.bukkit.entity.Player player) {
        // 6000 = 5min
        ClientboundUpdateMobEffectPacket entityEffect = new ClientboundUpdateMobEffectPacket(-player.getEntityId(), new MobEffectInstance(NIGHT_VISION, 6000, 2), false);
        ((CraftPlayer) player).getHandle().connection.send(entityEffect);
    }

    @Override
    public void removeNightVision(org.bukkit.entity.Player player) {
        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {

            ServerPlayer entityPlayer = ((CraftPlayer) player).getHandle();

            // Simply remove the entity effect
            ClientboundRemoveMobEffectPacket removeEntityEffect = new ClientboundRemoveMobEffectPacket(player.getEntityId(), NIGHT_VISION);
            entityPlayer.connection.send(removeEntityEffect);

            // resend the existing one
            MobEffectInstance mobEffect = entityPlayer.getEffect(NIGHT_VISION);
            ClientboundUpdateMobEffectPacket entityEffect = new ClientboundUpdateMobEffectPacket(player.getEntityId(), mobEffect, false);
            ((CraftPlayer) player).getHandle().connection.send(entityEffect);
            return;
        }

        // Simply remove the entity effect
        ClientboundRemoveMobEffectPacket removeEntityEffect = new ClientboundRemoveMobEffectPacket(player.getEntityId(), NIGHT_VISION);
        ((CraftPlayer) player).getHandle().connection.send(removeEntityEffect);
    }

    @Override
    public boolean isRemoveNightVisionPacket(PacketEvent event) {
        // 16 = night vision
        return ((ClientboundRemoveMobEffectPacket) event.getPacket().getHandle()).effect().value().equals(NIGHT_VISION.value());
    }
}