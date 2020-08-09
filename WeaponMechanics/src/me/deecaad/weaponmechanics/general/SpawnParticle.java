package me.deecaad.weaponmechanics.general;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.serializers.ColorSerializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.MaterialHelper;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;


/**
 * @deprecated Instead use Effects
 * @see me.deecaad.core.effects.types.ParticleEffect
 */
@Deprecated
public class SpawnParticle implements Serializer<SpawnParticle> {

    private List<ParticleData> particlesDatas;

    /**
     * Reflection support for 1.8 particles
     */
    private static Method sendParticles;
    private static Method worldGetHandle;

    /**
     * Empty constructor to be used as serializer
     */
    public SpawnParticle() {
        if (CompatibilityAPI.getVersion() < 1.09) {
            if (sendParticles == null) {
                sendParticles = ReflectionUtil.getMethod(ReflectionUtil.getNMSClass("WorldServer"), "sendParticles", ReflectionUtil.getNMSClass("EntityPlayer"), ReflectionUtil.getNMSClass("EnumParticle"), boolean.class, double.class, double.class, double.class, int.class, double.class, double.class, double.class, double.class, Array.newInstance(int.class, 0).getClass());
            }
            if (worldGetHandle == null) {
                worldGetHandle = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("CraftWorld"), "getHandle");
            }
        }
    }

    public SpawnParticle(List<ParticleData> particlesDatas) {
        this.particlesDatas = particlesDatas;
    }

    /**
     * @param entity the entity whose location is used to spawn particles
     */
    public void spawn(Entity entity) {
        spawn(entity.getLocation());
    }

    /**
     * @param location the location to spawn particles
     */
    public void spawn(Location location) {
        for (ParticleData particleData : this.particlesDatas) {
            Vector offset = particleData.particleOffset;
            location = particleData.locationFinder != null ? particleData.locationFinder.getNewLocation(location) : location;
            if (CompatibilityAPI.getVersion() >= 1.09) {
                location.getWorld().spawnParticle((org.bukkit.Particle) particleData.particle, location, particleData.particleAmount, offset.getX(), offset.getY(), offset.getZ(), particleData.particleSpeed, particleData.data);
                continue;
            }
            double x = location.getX(), y = location.getY(), z = location.getZ();
            Object worldServer = ReflectionUtil.invokeMethod(worldGetHandle, location.getWorld());
            ReflectionUtil.invokeMethod(sendParticles, worldServer, null, particleData.particle, true, x, y, z, particleData.particleAmount, offset.getX(), offset.getY(), offset.getZ(), particleData.particleSpeed, particleData.data);
        }
    }

    @Override
    public String getKeyword() {
        return "Particle";
    }

    @Override
    public SpawnParticle serialize(File file, ConfigurationSection configurationSection, String path) {
        ConfigurationSection particleSection = configurationSection.getConfigurationSection(path);
        if (particleSection == null) {
            return null;
        }
        List<ParticleData> particleDatas = new ArrayList<>();
        for (String particleName : particleSection.getKeys(false)) {
            ParticleData particleData = tryParticleData(file, configurationSection, path + "" + particleName);
            if (particleData == null) {
                continue;
            }
            particleDatas.add(particleData);
        }
        if (particleDatas.isEmpty()) {
            return null;
        }
        return new SpawnParticle(particleDatas);
    }

    @SuppressWarnings("unchecked")
    private ParticleData tryParticleData(File file, ConfigurationSection configurationSection, String path) {
        String stringParticle = configurationSection.getString(path + ".Particle");
        if (stringParticle == null) {
            return null;
        }
        stringParticle = stringParticle.toUpperCase();
        Object particle;
        try {
            if (CompatibilityAPI.getVersion() < 1.09) {
                particle = Enum.valueOf((Class<Enum>) ReflectionUtil.getNMSClass("EnumParticle"), stringParticle);
            } else {
                particle = org.bukkit.Particle.valueOf(stringParticle);
            }
        } catch (IllegalArgumentException e) {
            debug.log(LogLevel.ERROR,
                    "Found an invalid particle in configurations!",
                    "Located at file " + file + " in " + path + ".Particle (" + stringParticle + ") in configurations");
            return null;
        }
        String stringColor = configurationSection.getString(path + ".Extra_Data.Color");
        Color color = null;
        if (stringColor != null) {
            stringColor = stringColor.toUpperCase();
            if (!isColorable(stringParticle)) {
                debug.log(LogLevel.ERROR,
                        "Found an invalid configuration!",
                        "Located at file " + file + " in " + path + " in configurations",
                        "You can't use color with particle " + stringParticle);
                return null;
            } else {
                color = ColorSerializer.ColorType.fromString(stringColor);
                if (color == null) {
                    debug.log(LogLevel.ERROR,
                            "Found an invalid color in configurations!",
                            "Located at file " + file + " in " + path + ".Extra_Data.Color (" + stringColor + ") in configurations");
                    return null;
                }
            }
        }
        double particleSpeed = configurationSection.getDouble(path + ".Particle_Speed", -1);
        if (color != null && particleSpeed != -1) {
            if (CompatibilityAPI.getVersion() < 1.13 || (CompatibilityAPI.getVersion() >= 1.13 && !stringParticle.equals("REDSTONE"))) {
                debug.log(LogLevel.ERROR,
                        "Found an invalid configuration!",
                        "Located at file " + file + " in " + path + " in configurations",
                        "You can only use particle speed and color at same time when using REDSTONE in 1.13 and above versions");
                return null;
            }
        }
        if (particleSpeed == -1) {
            if (color != null) {
                particleSpeed = 1;
            } else {
                particleSpeed = 0;
            }
        }
        Object extraData = null;
        Vector offset = null;
        if (color != null) {
            if (CompatibilityAPI.getVersion() >= 1.13 && stringParticle.equals("REDSTONE")) {
                extraData = new org.bukkit.Particle.DustOptions(color, (float) 1.0);
            } else {
                float red = ColorSerializer.ColorType.getAsParticleColor(color.getRed());
                float green = ColorSerializer.ColorType.getAsParticleColor(color.getGreen());
                float blue = ColorSerializer.ColorType.getAsParticleColor(color.getBlue());
                offset = new Vector(red, green, blue);
            }
        }
        if (extraData == null) {
            String typeString = configurationSection.getString(path + ".Extra_Data.Type");
            if (typeString != null) {
                typeString = typeString.toUpperCase();
                if (!hasType(stringParticle)) {
                    debug.log(LogLevel.ERROR,
                            "Found an invalid configuration!",
                            "Located at file " + file + " in " + path + " in configurations",
                            "You can't use type with particle " + stringParticle,
                            "Only ITEM_CRACK, BLOCK_CRACK, BLOCK_DUST and FALLING_DUST are supported");
                    return null;
                }
                ItemStack type;
                try {
                    type = MaterialHelper.fromStringToItemStack(typeString);
                } catch (IllegalArgumentException e) {
                    debug.log(LogLevel.ERROR,
                            "Found an invalid material in configurations!",
                            "Located at file " + file + " in " + path + ".Extra_Data.Type (" + typeString + ") in configurations");
                    return null;
                }
                if (CompatibilityAPI.getVersion() >= 1.09) {
                    if (stringParticle.equals("ITEM_CRACK")) {
                        extraData = type;
                    } else if (CompatibilityAPI.getVersion() < 1.13) {
                        extraData = type.getData();
                    } else {
                        extraData = Bukkit.createBlockData(type.getType());
                    }
                } else {
                    if (stringParticle.equals("ITEM_CRACK")) {
                        extraData = new int[] { type.getType().getId(), type.getDurability() };
                    } else if (stringParticle.equals("BLOCK_CRACK") || stringParticle.equals("BLOCK_DUST")) {
                        extraData = new int[] { type.getData().getItemType().getId() + (type.getData().getData() << 12) };
                    }
                }
            }
        }
        if (extraData == null && (hasType(stringParticle) || (CompatibilityAPI.getVersion() >= 1.13 && stringParticle.equals("REDSTONE")))) {
            debug.log(LogLevel.ERROR,
                    "Found an invalid configuration!",
                    "Located at file " + file + " in " + path + " in configurations",
                    "Particle " + stringParticle + " requires more data",
                    "Either type or color should be specified");
            return null;
        }
        if (CompatibilityAPI.getVersion() < 1.09 && extraData == null) {
            extraData = new int[0];
        }
        int particleAmount = configurationSection.getInt(path + ".Particle_Amount", 1);
        return new ParticleData(new LocationFinder().serialize(file, configurationSection, path + ".Location_Finder"), particle, particleAmount, particleSpeed, offset, extraData);
    }

    private boolean hasType(String particle) {
        return particle.equals("ITEM_CRACK") || particle.equals("BLOCK_CRACK") || particle.equals("BLOCK_DUST") || particle.equals("FALLING_DUST");
    }

    private boolean isColorable(String particle) {
        return particle.equals("REDSTONE") || particle.equals("SPELL_MOB") || particle.equals("SPELL_MOB_AMBIENT");
    }

    public static class ParticleData {

        private LocationFinder locationFinder;
        private Object particle;
        private int particleAmount;
        private double particleSpeed;
        private Vector particleOffset;
        private Object data;

        public ParticleData(LocationFinder locationFinder, Object particle, int particleAmount, double particleSpeed, Vector particleOffset, Object data) {
            this.locationFinder = locationFinder;
            this.particle = particle;
            this.particleAmount = particleAmount;
            this.particleSpeed = particleSpeed;
            if (particleOffset == null) {
                particleOffset = new Vector(0.0, 0.0, 0.0);
            }
            this.particleOffset = particleOffset;
            this.data = data;
        }

        /**
         * @return the location finder for particles
         */
        public LocationFinder getLocationFinder() {
            return this.locationFinder;
        }

        /**
         * In 1.8 this returns NMS EnumParticle, but in 1.9 and later Bukkit Particle.
         *
         * @return the particle used
         */
        public Object getParticle() {
            return this.particle;
        }

        /**
         * @return the particle amount when spawning
         */
        public int getParticleAmount() {
            return this.particleAmount;
        }

        /**
         * @return the particle speed when spawning
         */
        public double getParticleSpeed() {
            return this.particleSpeed;
        }

        /**
         * Before 1.13 this most likely contains colors for particles.
         *
         * @return the particle offset when spawning
         */
        public Vector getParticleOffset() {
            return this.particleOffset;
        }

        /**
         * This may be material data, block data, integer array or something else.
         * Make sure you're using right data for particle.
         *
         * @return the data used
         */
        public Object getData() {
            return this.data;
        }
    }
}
