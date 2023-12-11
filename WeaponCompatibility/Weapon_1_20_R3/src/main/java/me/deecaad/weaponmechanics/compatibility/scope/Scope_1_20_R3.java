package me.deecaad.weaponmechanics.compatibility.scope;

import com.comphenix.protocol.events.PacketEvent;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Abilities;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.potion.CraftPotionEffectType;
import org.bukkit.potion.PotionEffectType;

public class Scope_1_20_R3 implements IScopeCompatibility {

    private static final MobEffect NIGHT_VISION = CraftPotionEffectType.bukkitToMinecraft(PotionEffectType.NIGHT_VISION);

    static {
        if (ReflectionUtil.getMCVersion() != 20) {
            WeaponMechanics.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Scope_1_20_R3.class + " when not using Minecraft 20",
                    new InternalError()
            );
        }
    }

    @Override
    public void updateAbilities(org.bukkit.entity.Player player) {
        ServerPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        Abilities abilities = entityPlayer.getAbilities();
        entityPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(abilities));
    }

    @Override
    public void addNightVision(org.bukkit.entity.Player player) {
        // 6000 = 5min
        ClientboundUpdateMobEffectPacket entityEffect = new ClientboundUpdateMobEffectPacket(-player.getEntityId(), new MobEffectInstance(NIGHT_VISION, 6000, 2));
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
            ClientboundUpdateMobEffectPacket entityEffect = new ClientboundUpdateMobEffectPacket(player.getEntityId(), mobEffect);
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
        return ((ClientboundRemoveMobEffectPacket) event.getPacket().getHandle()).getEffect() == NIGHT_VISION;
    }
}