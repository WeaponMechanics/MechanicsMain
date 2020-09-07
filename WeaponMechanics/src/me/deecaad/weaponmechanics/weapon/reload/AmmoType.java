package me.deecaad.weaponmechanics.weapon.reload;

import me.deecaad.core.file.Serializer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public interface AmmoType extends Serializer<AmmoType> {

    /**
     * Should try to take an <code>amount</code> of this <code>AmmoType</code>
     * from the given <code>shooter</code>. If the player has the proper
     * <code>amount</code> of ammo, than this method should return
     * <code>true</code> (Otherwise <code>false</code>).
     *
     * This <code>AmmoType</code> should look in the main <code>Configuration</code>
     * using the given <code>weaponTitle</code> to check for options.
     *
     * If this <code>AmmoType</code> is only applicable to a <code>Player</code>,
     * than make sure to return <code>true</code> for all other entities.
     *
     *
     * @param shooter Who is shooting the gun
     * @param gun The gun being shot
     * @param weaponTitle The weaponTitle of the gun
     * @param amount Amount of ammo being shot
     * @return true if shooter has ammo
     */
    boolean takeAmmo(@Nonnull LivingEntity shooter, @Nonnull ItemStack gun, @Nonnull String weaponTitle, @Nonnegative int amount);
}