package me.deecaad.weaponmechanics.commands.testcommands;

import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.core.effects.Effect;
import me.deecaad.core.effects.types.ShapedParticleEffect;
import me.deecaad.core.effects.shapes.Shape;
import me.deecaad.core.effects.shapes.Spiral;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.events.ProjectileMoveEvent;
import me.deecaad.weaponmechanics.weapon.projectile.ICustomProjectile;
import me.deecaad.weaponmechanics.weapon.projectile.Projectile;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileMotion;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

@CommandPermission(permission = "weaponmechanics.commands.test.shoot")
public class ShootCommand extends SubCommand implements Listener {

    public ShootCommand() {
        super("wm test", "shoot", "Shoot with given values", "<10.0,20.0,30.0,40.0,50.0> <entity-type>");

        Bukkit.getPluginManager().registerEvents(this, WeaponMechanics.getPlugin());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        Player player = (Player) sender;

        ProjectileMotion projectileMotion = new ProjectileMotion(0.05, -1, false, -1, false, 0.99, 0.96, 0.98);
        double speed = Double.parseDouble(args[0]) * 0.1;
        if (args.length > 1) {
            EntityType entityType = EntityType.valueOf(args[1].toUpperCase());
            Projectile projectile = new Projectile(projectileMotion, entityType, 0.25f, 0.25f, null, null);
            projectile.shoot(player, player.getEyeLocation(), player.getLocation().getDirection().multiply(speed));
            return;
        }

        Projectile projectile = new Projectile(projectileMotion, null, 0.25f, 0.25f, null, null);
        projectile.shoot(player, player.getEyeLocation(), player.getLocation().getDirection().multiply(speed));
    }

    @Override
    public List<String> handleCustomTag(String[] args, String current) {
        switch (current) {
            case "<entity-type>":
                return entityTypesToList();
            default:
                return super.handleCustomTag(args, current);
        }
    }

    private List<String> entityTypesToList() {
        List<String> entityTypes = new ArrayList<>();
        for (EntityType type : EntityType.values()) {
            entityTypes.add(type.name());
        }
        return entityTypes;
    }

    Shape[] shapes = new Shape[] {
            new Spiral(64, 2, 0 * Math.PI / 3, null, 1),
            new Spiral(64, 2, 1 * Math.PI / 3, null, 1),
            new Spiral(64, 2, 2 * Math.PI / 3, null, 1),
            new Spiral(64, 2, 3 * Math.PI / 3, null, 1),
            new Spiral(64, 2, 4 * Math.PI / 3, null, 1),
            new Spiral(64, 2, 5 * Math.PI / 3, null, 1)
    };

    Object[] data = new Object[] {
            new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1f),
            new Particle.DustOptions(Color.fromRGB(255, 165, 0), 1f),
            new Particle.DustOptions(Color.fromRGB(255, 255, 0), 1f),
            new Particle.DustOptions(Color.fromRGB(0, 128, 0), 1f),
            new Particle.DustOptions(Color.fromRGB(0, 0, 255), 1f),
            new Particle.DustOptions(Color.fromRGB(238, 130, 238), 1f),
    };

    // Only use if needed for testing
    //@EventHandler
    public void onMove(ProjectileMoveEvent e) {
        ICustomProjectile projectile = e.getCustomProjectile();
        Vector location = projectile.getLastLocation();
        Vector between = projectile.getLocation().subtract(projectile.getLastLocation());

        for (int i = 0; i < shapes.length; i++) {
            Shape shape = shapes[i];
            Object data = this.data[i];

            shape.setAxis(between);
            Effect spiral = new ShapedParticleEffect(Particle.REDSTONE, 1, 0, 0, 0, data, shape, 0);
            spiral.spawn(WeaponMechanics.getPlugin(), projectile.getWorld(), location.getX(), location.getY(), location.getZ());
        }
    }
}