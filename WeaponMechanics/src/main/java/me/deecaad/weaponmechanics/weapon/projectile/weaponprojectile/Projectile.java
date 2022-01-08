package me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile;

import me.deecaad.core.file.Serializer;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;

public class Projectile implements Serializer<Projectile> {

    private ProjectileSettings projectileSettings;
    private Sticky sticky;
    private Through through;
    private Bouncy bouncy;
    private Mechanics mechanics;

    /**
     * Empty constructor to be used as serializer
     */
    public Projectile() { }

    public Projectile(ProjectileSettings projectileSettings, Sticky sticky, Through through, Bouncy bouncy, Mechanics mechanics) {
        this.projectileSettings = projectileSettings;
        this.sticky = sticky;
        this.through = through;
        this.bouncy = bouncy;
        this.mechanics = mechanics;
    }

    /**
     * Shoots this projectile with given location and motion
     *
     * @param shooter the living entity used to shoot
     * @param location the location from where to shoot
     * @param motion the motion of projectile
     * @param weaponStack the weapon stack used to shoot
     * @param weaponTitle the weapon title used to shoot
     */
    public WeaponProjectile shoot(LivingEntity shooter, Location location, Vector motion, ItemStack weaponStack, String weaponTitle) {
        WeaponProjectile projectile = new WeaponProjectile(projectileSettings, shooter, location, motion, weaponStack, weaponTitle, sticky, through, bouncy);
        WeaponMechanics.getProjectilesRunnable().addProjectile(projectile);
        if (mechanics != null) mechanics.use(new CastData(projectile));
        return projectile;
    }

    @Override
    public String getKeyword() {
        return "Projectile";
    }

    @Override
    public Projectile serialize(File file, ConfigurationSection configurationSection, String path) {
        ProjectileSettings projectileSettings = new ProjectileSettings().serialize(file, configurationSection, path + ".Projectile_Settings");
        if (projectileSettings == null) return null;

        Sticky sticky = new Sticky().serialize(file, configurationSection, path + ".Sticky");
        Through through = new Through().serialize(file, configurationSection, path + ".Through");
        Bouncy bouncy = new Bouncy().serialize(file, configurationSection, path + ".Bouncy");
        Mechanics mechanics = new Mechanics().serialize(file, configurationSection, path + ".Mechanics");
        return new Projectile(projectileSettings, sticky, through, bouncy, mechanics);
    }
}