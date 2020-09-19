package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

public class WeaponShootEvent extends WeaponEvent implements Cancellable {

    private ICustomProjectile projectile;
    private boolean isCancelled;

    public WeaponShootEvent(String weaponTitle, ItemStack weaponItem, LivingEntity weaponUser, ICustomProjectile projectile) {
        super(weaponTitle, weaponItem, weaponUser);

        this.projectile = projectile;
    }

    public ICustomProjectile getProjectile() {
        return projectile;
    }

    public void setProjectile(ICustomProjectile projectile) {
        this.projectile = projectile;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }
}
