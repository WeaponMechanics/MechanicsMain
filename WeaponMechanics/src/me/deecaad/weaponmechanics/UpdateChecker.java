package me.deecaad.weaponmechanics;

import me.deecaad.core.web.AUpdateChecker;
import me.deecaad.core.web.SpigotResource;
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

        char majorColor = majorsBehind >= 2 ? 'c' : majorsBehind >= 1 ? 'e' : 'a';
        char minorColor = minorsBehind >= 10 ? 'c' : minorsBehind >= 5 ? 'e' : 'a';
        char patchColor = patchesBehind >= 5 ? 'c' : patchesBehind >= 3 ? 'e' : 'a';

        sender.sendMessage("§7➢  §6There is an update available for WeaponMechanics");
        sender.sendMessage("§7➢    §6Major versions behind: " + majorColor + majorsBehind);
        sender.sendMessage("§7➢    §6Minor versions behind: " + minorColor + minorsBehind);
        sender.sendMessage("§7➢    §6Patch versions behind: " + patchColor + patchesBehind);
    }
}
