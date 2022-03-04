package me.deecaad.weaponmechanics.commands.testcommands;

import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.Projectile;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.ProjectileSettings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@CommandPermission(permission = "weaponmechanics.commands.test.shoot")
public class ShootCommand extends SubCommand {

    public ShootCommand() {
        super("wm test", "shoot", "Shoot with given values", "<10.0,20.0,30.0,40.0,80.0> <entity-type> <0.05>");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        Player player = (Player) sender;
        double speed = Double.parseDouble(args[0]) / 20;
        double gravity = 0.05;
        EntityType entityType = null;
        if (args.length > 1) {
            entityType = EntityType.valueOf(args[1].toUpperCase());
        }
        if (args.length > 2) {
            gravity = Double.parseDouble(args[1]);
        }

        ProjectileSettings projectileSettings = new ProjectileSettings(entityType, null,
                gravity, false, -1, false,
                -1, 0.99, 0.96, 0.98, false, 600, -1);
        Projectile projectile = new Projectile(projectileSettings, null, null, null, null);
        projectile.shoot(player, player.getEyeLocation(), player.getLocation().getDirection().multiply(speed), null, null);
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
}