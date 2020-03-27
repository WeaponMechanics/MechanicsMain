package me.deecaad.weaponmechanics.utils;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.placeholder.PlaceholderHandler;
import me.deecaad.weaponmechanics.general.AddPotionEffect;
import me.deecaad.weaponmechanics.general.PlaySound;
import me.deecaad.weaponmechanics.general.SendMessage;
import me.deecaad.weaponmechanics.general.SpawnFirework;
import me.deecaad.weaponmechanics.particles.SpawnParticle;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

import java.util.Map;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class UsageHelper {

    /**
     * Don't let anyone instantiate this class
     */
    private UsageHelper() { }

    /**
     * Uses sound, firework, particles, message and potion effect if they're not null
     *
     * @param path path in the configurations
     * @param livingEntity uses everything in this general holder to specific living entity and its location
     * @param weaponStack if required for PlaceholderAPI checking
     * @param weaponTitle if required for PlaceholderAPI checking
     * @param tempPlaceholders temporary placeholders
     */
    public static void useGeneral(String path, LivingEntity livingEntity, @Nullable ItemStack weaponStack, @Nullable String weaponTitle, @Nullable Map<String, PlaceholderHandler> tempPlaceholders) {
        Configuration config = getConfigurations();

        PlaySound sound = config.getObject(path + ".Sound", PlaySound.class);
        if (sound != null) sound.play(livingEntity);

        SpawnFirework firework = config.getObject(path + ".Firework", SpawnFirework.class);
        if (firework != null) firework.spawn(livingEntity);

        SpawnParticle particle = config.getObject(path + ".Particle", SpawnParticle.class);
        if (particle != null) particle.spawn(livingEntity);

        if (livingEntity.getType() == EntityType.PLAYER) {
            SendMessage message = config.getObject(path + ".Message", SendMessage.class);
            if (message != null) message.send(false, (Player) livingEntity, weaponStack, weaponTitle, tempPlaceholders);
        }

        AddPotionEffect potionEffect = config.getObject(path + ".Potion_Effect", AddPotionEffect.class);
        if (potionEffect != null) potionEffect.add(livingEntity);
    }

    /**
     * Uses sound, firework and particle if they're not null
     *
     * @param path path in the configurations
     * @param location uses everything in this general holder to specific location
     */
    public static void useGeneral(String path, Location location) {
        Configuration config = getConfigurations();

        PlaySound sound = config.getObject(path + ".Sound", PlaySound.class);
        if (sound != null) sound.play(location);

        SpawnFirework firework = config.getObject(path + ".Firework", SpawnFirework.class);
        if (firework != null) firework.spawn(location);

        SpawnParticle particle = config.getObject(path + ".Particle", SpawnParticle.class);
        if (particle != null) particle.spawn(location);
    }
}