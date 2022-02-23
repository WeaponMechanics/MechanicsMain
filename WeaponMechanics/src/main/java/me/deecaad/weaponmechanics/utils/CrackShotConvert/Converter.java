package me.deecaad.weaponmechanics.utils.CrackShotConvert;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Converter {

    private final CommandSender sender;
    private CrackShotConverter cs;
    private CrackShotPlusConverter csp;

    public Converter(CommandSender sender) {
        this.sender = sender;

        sender.sendMessage(ChatColor.GREEN + "Starting CrackShot and CrackShotPlus conversion...");

        if (Bukkit.getPluginManager().getPlugin("CrackShot") == null) {
            sender.sendMessage(ChatColor.RED + "Could not find CrackShot!");
            return;
        }

        cs = new CrackShotConverter();
        sender.sendMessage(ChatColor.GREEN + "Found CrackShot");
        if (Bukkit.getPluginManager().getPlugin("CrackShotPlus") != null) {
            csp = new CrackShotPlusConverter();
            sender.sendMessage(ChatColor.GREEN + "Found CrackShotPlus");
        } else {
            sender.sendMessage(ChatColor.RED + "Could not find CrackShotPlus!");
        }
    }

    public void convertAllFiles(File outputDirectory) {
        File crackShotDirectory = new File("plugins/CrackShot/");
        if (crackShotDirectory == null || crackShotDirectory.listFiles() == null) {
            sender.sendMessage(ChatColor.RED + "Could not find CrackShot directory or it was empty!");
            return;
        }
        int counter = convertAllFiles(crackShotDirectory, outputDirectory);
        sender.sendMessage(ChatColor.GREEN + "Successfully converted " + counter + " weapons to WeaponMechanics!");
        sender.sendMessage(ChatColor.GREEN + "CrackShot" + (csp == null ? " " : " and CrackShotPlus ") + "configurations converted to folder WeaponMechanics/crackshotconvert/");
    }

    public int convertAllFiles(File directory, File outputDirectory) {
        if (directory == null || directory.listFiles() == null) {
            throw new IllegalArgumentException("The given file MUST be a directory!");
        }

        int counter = 0;
        for (File directoryFile : directory.listFiles()) {
            if (directoryFile.isDirectory()) {
                counter += convertAllFiles(directory, outputDirectory);
            } else if (directoryFile.getName().endsWith(".yml")
                    && !directoryFile.getName().startsWith("messages")
                    && !directoryFile.getName().startsWith("general")) {
                counter += convertOneFile(directoryFile, outputDirectory);
            }
        }
        return counter;
    }

    public int convertOneFile(File file, File outputDirectory) {
        YamlConfiguration outputConfiguration = new YamlConfiguration();

        int counter = 0;
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        for (String key : configuration.getKeys(false)) {

            try {
                cs.convertOneKey(configuration, key, outputConfiguration);
                if (csp != null) csp.convertOneKey(configuration, key, outputConfiguration);
                ++counter;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String pathInOutput = outputDirectory.getPath() + "/" + file.getName();
        try {
            outputConfiguration.save(new File(pathInOutput));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return counter;
    }
}