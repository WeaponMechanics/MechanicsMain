package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.WeaponMechanicsAPI;
import me.deecaad.weaponmechanics.lib.CrackShotConvert.Converter;
import org.bukkit.command.CommandSender;

import java.io.File;

@CommandPermission(permission = "weaponmechanics.commands.convert")
public class CrackShotConvertCommand extends SubCommand {

    public CrackShotConvertCommand() {
        super("wm", "convert", "Converts CrackShot and CrackShotPlus files to WeaponMechanics");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        WeaponMechanics plugin = WeaponMechanicsAPI.getInstance();
        File outputPath = new File(plugin.getDataFolder().getPath() + "/weapons/crackshotconvert/");
        WeaponMechanics.getInstance().getFoliaScheduler().async().runNow(task -> new Converter(sender).convertAllFiles(outputPath));
    }
}