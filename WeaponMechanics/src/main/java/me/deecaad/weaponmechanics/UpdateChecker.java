package me.deecaad.weaponmechanics;

import me.deecaad.core.web.AUpdateChecker;
import me.deecaad.core.web.SpigotResource;
import me.deecaad.weaponmechanics.commands.InfoCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class UpdateChecker extends AUpdateChecker {

    public UpdateChecker(SpigotResource spigotResource, int requiredMajorVersionsBehind, int requiredMinorVersionsBehind, int requiredPatchVersionsBehind) {
        super(spigotResource, requiredMajorVersionsBehind, requiredMinorVersionsBehind, requiredPatchVersionsBehind);
    }

    @Override
    public void onUpdateFound(CommandSender sender, SpigotResource spigotResource) {
        if (!sender.hasPermission("weaponmechanics.admin")) return;

        int majorsBehind = spigotResource.getMajorVersionsBehind();
        int minorsBehind = spigotResource.getMinorVersionsBehind();
        int patchesBehind = spigotResource.getPatchVersionsBehind();

        String majorColor = ChatColor.translateAlternateColorCodes('&',
                "&" + (majorsBehind >= 2 ? 'c': majorsBehind >= 1 ? 'e' : 'a'));
        String minorColor = ChatColor.translateAlternateColorCodes('&',
                "&" + (minorsBehind >= 10 ? 'c': minorsBehind >= 5 ? 'e' : 'a'));
        String patchColor = ChatColor.translateAlternateColorCodes('&',
                "&" + (patchesBehind >= 5 ? 'c': patchesBehind >= 3 ? 'e' : 'a'));

        sender.sendMessage("" + ChatColor.GRAY + InfoCommand.SYM + ChatColor.GOLD + "  There is an update available for WeaponMechanics");
        if (majorsBehind > 0) {
            sender.sendMessage("" + ChatColor.GRAY + InfoCommand.SYM + ChatColor.GOLD + "    Major versions behind: " + majorColor + majorsBehind);
        } else if (minorsBehind > 0) {
            sender.sendMessage("" + ChatColor.GRAY + InfoCommand.SYM + ChatColor.GOLD + "    Minor versions behind: " + minorColor + minorsBehind);
        } else {
            sender.sendMessage("" + ChatColor.GRAY + InfoCommand.SYM + ChatColor.GOLD + "    Patch versions behind: " + patchColor + patchesBehind);
        }
    }
}
