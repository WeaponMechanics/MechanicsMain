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
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Method;

public class Scope_1_19_R1 implements IScopeCompatibility {

    static {
        if (ReflectionUtil.getMCVersion() != 19) {
            WeaponMechanics.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Scope_1_19_R1.class + " when not using Minecraft 19",
                    new InternalError()
            );
        }
    }

    private final boolean is1_19;
    private Method abilitiesMethod;

    public Scope_1_19_R1() {
        is1_19 = !Bukkit.getServer().getVersion().contains("1.19.1") && !Bukkit.getServer().getVersion().contains("1.19.1");
        if (is1_19) {
            abilitiesMethod = ReflectionUtil.getMethod(Player.class, "fC");
        }
    }

    @Override
    public void updateAbilities(org.bukkit.entity.Player player) {
        ServerPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        // In 1.19, the method is `fC()`. In 1.19.1 it is `fB()`. Due to how paper
        // remaps the method, we cannot support both at the same time. Since we
        // are using 1.19.1 as the remapper, we have to use reflection in 1.19
        Abilities abilities = is1_19 ? (Abilities) ReflectionUtil.invokeMethod(abilitiesMethod, entityPlayer) : entityPlayer.getAbilities();

        entityPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(abilities));
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
    public boolean isRemoveNightVisionPacket(PacketEvent event) {
        // 16 = night vision
        return ((ClientboundRemoveMobEffectPacket) event.getPacket().getHandle()).getEffect() == MobEffect.byId(16);
    }
}