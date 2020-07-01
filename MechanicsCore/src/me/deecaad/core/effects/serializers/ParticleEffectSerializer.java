package me.deecaad.core.effects.serializers;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.effects.Effect;
import me.deecaad.core.effects.shapes.Shape;
import me.deecaad.core.effects.types.ParticleEffect;
import me.deecaad.core.effects.types.ShapedParticleEffect;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.serializers.ColorSerializer;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.utils.StringUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static me.deecaad.core.MechanicsCore.debug;

public class ParticleEffectSerializer implements Serializer<List<ParticleEffect>> {

    /**
     * Empty constructor for serializer
     */
    public ParticleEffectSerializer() {
    }

    @Override
    public String getKeyword() {
        return "Particles";
    }

    @Override
    public List<ParticleEffect> serialize(File file, ConfigurationSection configurationSection, String path) {

        List<ParticleEffect> temp = new ArrayList<>();

        for (String key : configurationSection.getConfigurationSection(path).getKeys(false)) {
            ConfigurationSection config = configurationSection.getConfigurationSection(path + "." + key);

            String foundAt = StringUtils.foundAt(file, path + "." + key);

            Particle particle;
            if (!config.isString("Type")) {
                debug.error("You must specify a String particle!",
                        "You can find a complete list of these at: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Particle.html",
                        foundAt);
                continue;
            }

            try {
                particle = Particle.valueOf(config.getString("Type").toUpperCase());
            } catch (EnumConstantNotPresentException ex) {
                debug.error("Unknown Particle: " + config.getString("Type"), foundAt);
                continue;
            }

            int amount = config.getInt("Amount", 1);
            double hSpread = config.getDouble("Horizontal_Spread", 0.0);
            double vSpread = config.getDouble("Vertical_Spread", hSpread);
            double speed = config.getDouble("Speed", 0);
            int interval = config.getInt("Interval", 0);

            Object data;
            if (particle.getDataType() == ItemStack.class && config.contains("Extra_Data.Item")) {
                data = new ItemSerializer().serialize(file, configurationSection, path + "." + key + ".Extra_Data.Item");

            } else if (particle.getDataType() == Particle.DustOptions.class && config.contains("Extra_Data.Color")) {
                Color color = new ColorSerializer().serialize(file, configurationSection, path + "." + key + ".Extra_Data.Color");
                float size = (float) config.getInt("Extra_Data.Size");
                data = new Particle.DustOptions(color, size);

            } else if (CompatibilityAPI.getVersion() >= 1.13
                    && particle.getDataType() == BlockData.class
                    && config.contains("Extra_Data.Block")) {

                Material mat;
                try {
                    mat = Material.valueOf(config.getString("Extra_Data.Block"));
                } catch (EnumConstantNotPresentException ex) {
                    debug.error("Unknown material \"" + config.getString("Extra_Data.Block") + "\"");
                    continue;
                }

                data = mat.createBlockData();

            } else if (CompatibilityAPI.getVersion() < 1.13
                    && particle.getDataType() == MaterialData.class
                    && config.contains("Extra_Data.Block")) {

                String[] split = StringUtils.split(config.getString("Extra_Data.Block"));
                Material mat;
                byte b;
                try {
                    mat = Material.valueOf(split[0].toUpperCase());
                    b = split.length > 1 ? Byte.parseByte(split[1]) : 0;
                } catch (EnumConstantNotPresentException ex) {
                    debug.error("Unknown material type \"" + split[0] + "\"",
                            "You are using legacy materials (Server 1.12.2 and lower)",
                            StringUtils.foundAt(file, path + "." + key + ".Extra_Data.Block"));
                    continue;
                } catch (NumberFormatException ex) {
                    debug.error("Expected to find a number, but instead found \"" + split[1] + "\"",
                            StringUtils.foundAt(file, path + "." + key + ".Extra_Data.Block"));
                    continue;
                }

                data = new MaterialData(mat, b);

            } else {
                data = null;
            }

            ParticleEffect effect;
            if (config.contains("Shape")) {
                Shape shape = new ShapeSerializer().serialize(file, configurationSection, path + "." + key + ".Shape");
                effect = new ShapedParticleEffect(particle, amount, hSpread, vSpread, speed, data, shape, interval);
            } else {
                effect = new ParticleEffect(particle, amount, hSpread, vSpread, speed, data);
            }
            temp.add(effect);
        }

        return temp;
    }
}
