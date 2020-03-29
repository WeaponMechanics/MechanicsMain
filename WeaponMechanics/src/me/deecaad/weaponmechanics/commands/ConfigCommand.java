package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.commands.SubCommand;
import me.deecaad.core.utils.DebugUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtils;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.general.ItemSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class ConfigCommand extends SubCommand {
    
    public ConfigCommand() {
        super("wm", "config", "Translateds things into yml for config", SUB_COMMANDS);
        
        File file = new File(WeaponMechanics.getPlugin().getDataFolder(), "deserializers.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                DebugUtil.log(LogLevel.ERROR, "Failed to write to file \"" + file.getName() + "\"",
                        "Try restarting the server");
            }
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        commands.register(new ItemCommand(config));
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0) {
            commands.execute(args[0], sender, Arrays.copyOfRange(args, 1, args.length));
            return;
        }
        sender.sendMessage(StringUtils.color(toString()));
    }
    
    private static class ItemCommand extends SubCommand {
        
        private FileConfiguration file;
        
        ItemCommand(FileConfiguration file) {
            super("wm config", "item", "Translates held item into config");
            this.file = file;
        }
    
        @Override
        public void execute(CommandSender sender, String[] args) {
            if (!(sender instanceof Player)) return;
            
            int i = 0;
            while (file.contains("" + i)) {
                i++;
            }
            ItemStack item = ((Player) sender).getInventory().getItemInMainHand();
            new ItemSerializer().deserialize(i + "", item).forEach(file::set);
        }
    }
}
