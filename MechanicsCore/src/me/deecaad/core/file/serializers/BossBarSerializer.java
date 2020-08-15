package me.deecaad.core.file.serializers;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.Enums;
import me.deecaad.core.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static me.deecaad.core.MechanicsCore.debug;

public class BossBarSerializer implements Serializer<BossBar> {

    public BossBarSerializer() {
    }

    @Override
    public String getKeyword() {
        return "Boss_Bar";
    }

    @Override
    public BossBar serialize(File file, ConfigurationSection configurationSection, String path) {
        ConfigurationSection config = configurationSection.getConfigurationSection(path);

        String title = config.getString("Title");
        BarColor color = Enums.getIfPresent(BarColor.class, config.getString("Color")).orElseGet(() -> {
            String input = config.getString("Color");
            if (input == null || input.isEmpty())
                debug.error("You forget to give Boss_Bar a Color!", StringUtils.foundAt(file, path));

            debug.error("Unknown color: " + input + ", did you mean "
                    + StringUtils.didYouMean(input, Enums.getOptions(BarColor.class)),
                    StringUtils.foundAt(file, path));
            return BarColor.WHITE;
        });
        BarStyle style = Enums.getIfPresent(BarStyle.class, config.getString("Style")).orElseGet(() -> {
            String input = config.getString("Style");
            if (input == null || input.isEmpty())
                debug.error("You forgot to give Boss_Bar a style!", StringUtils.foundAt(file, path));

            debug.error("Unknown style: " + config.getString("Color"));
            return BarStyle.SOLID;
        });

        List<BarFlag> flags = new ArrayList<>();
        if (config.getBoolean("Darken_Sky")) flags.add(BarFlag.DARKEN_SKY);
        if (config.getBoolean("Show_Fog")) flags.add(BarFlag.CREATE_FOG);
        if (config.getBoolean("Play_Music")) flags.add(BarFlag.PLAY_BOSS_MUSIC);

        return Bukkit.createBossBar(title, color, style, flags.toArray(new BarFlag[0]));
    }
}
