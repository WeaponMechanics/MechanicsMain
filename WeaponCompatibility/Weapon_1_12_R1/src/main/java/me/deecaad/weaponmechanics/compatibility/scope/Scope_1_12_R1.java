package me.deecaad.weaponmechanics.compatibility.scope;

import com.comphenix.protocol.events.PacketEvent;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.MobEffect;
import net.minecraft.server.v1_12_R1.MobEffectList;
import net.minecraft.server.v1_12_R1.MobEffects;
import net.minecraft.server.v1_12_R1.PacketPlayOutAbilities;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityEffect;
import net.minecraft.server.v1_12_R1.PacketPlayOutRemoveEntityEffect;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;

public class Scope_1_12_R1 implements IScopeCompatibility {

    private static final Field effectsField;

    static {
        Class<?> effectsPacket = ReflectionUtil.getPacketClass("PacketPlayOutRemoveEntityEffect");

        effectsField = ReflectionUtil.getField(effectsPacket, "b");

        if (ReflectionUtil.getMCVersion() != 12) {
            WeaponMechanics.debug.log(
                    LogLevel.ERROR,
                    "Loaded " + Scope_1_12_R1.class + " when not using Minecraft 12",
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
    public boolean isRemoveNightVisionPacket(PacketEvent event) {
        return ReflectionUtil.invokeField(effectsField, event.getPacket().getHandle()) == MobEffects.NIGHT_VISION;
    }
}