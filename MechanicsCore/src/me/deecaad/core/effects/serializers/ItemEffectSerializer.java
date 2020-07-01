package me.deecaad.core.effects.serializers;

import me.deecaad.core.effects.types.ItemEffect;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.utils.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static me.deecaad.core.MechanicsCore.debug;

public class ItemEffectSerializer implements Serializer<List<ItemEffect>> {

    @Override
    public String getKeyword() {
        return "Items";
    }

    @Override
    public List<ItemEffect> serialize(File file, ConfigurationSection configurationSection, String path) {
        ConfigurationSection config = configurationSection.getConfigurationSection(path);

        List<ItemEffect> temp = new ArrayList<>();

        for (String key : config.getKeys(false)) {
            String pathTo = path + "." + key;

            ItemStack item = new ItemSerializer().serialize(file, configurationSection, pathTo + ".Item");

            if (item == null) {
                debug.warn("Error occurred while making itemstack for effects", StringUtils.foundAt(file, pathTo + ".Item"));
                continue;
            }

            int ticksAlive = config.getInt(key + ".Ticks_Alive", 200);

            ItemEffect effect = new ItemEffect(item, ticksAlive);
            temp.add(effect);
        }

        return temp;
    }
}
