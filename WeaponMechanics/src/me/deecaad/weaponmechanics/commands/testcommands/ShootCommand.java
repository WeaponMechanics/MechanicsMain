package me.deecaad.weaponmechanics.commands.testcommands;

import me.deecaad.core.commands.SubCommand;
import me.deecaad.weaponmechanics.weapon.projectile.Projectile;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileMotion;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ShootCommand extends SubCommand {

    public ShootCommand() {
        super("wm test", "shoot", "Shoot with given values", "<speed>, <entity-type>");
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
        Bukkit.broadcastMessage("" + current);
        switch (current) {
            case "<entity-type>": // Starts with doesn't work in these
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