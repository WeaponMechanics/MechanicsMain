package me.deecaad.weaponmechanics.weapon.shoot.spread;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ProbabilityMap;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.ArrayUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

/**
 * This class serves to get random
 * locations from points on an image.
 *
 * A point is more likely to be selected
 * if it has less color (more black)
 *
 * All SpreadImages should be constructed
 * onLoad or onEnable, as loading the image
 * from a file (IO operation) can be fairly
 * taxing.
 */
public class SpreadImage implements Serializer<SpreadImage> {
    
    private ProbabilityMap<Point> points;
    private int height, width;
    
    /**
     * Default constructor for serialization
     */
    public SpreadImage() {}
    
    public SpreadImage(Sprite sprite, int fovWidth, int fovHeight) {
        this.points = new ProbabilityMap<>();

        width = sprite.getWidth();
        height = sprite.getHeight();

        double maxYaw = Math.toRadians(fovWidth);
        double maxPitch = Math.toRadians(fovHeight);

        double xMiddle = width / 2.0;
        double yMiddle = height / 2.0;
        
        int[][] pixels = ArrayUtils.toBlackAndWhite(sprite.getPixels());
        for (int y = 0; y < pixels.length; y++) {
            for (int x = 0; x < pixels[y].length; x++) {
                if (pixels[y][x] != 0) {
                    double yaw = (x - xMiddle) / sprite.getWidth() * maxYaw;
                    double pitch = (y - yMiddle) / sprite.getHeight() * maxPitch;

                    Point point = new Point(yaw, pitch);
                    double chance = pixels[y][x] / 255d;
                    points.add(point, chance);
                }
            }
        }
    }
    
    public int getHeight() {
        return height;
    }
    
    public int getWidth() {
        return width;
    }

    /**
     * Gets a given number of random points (x, y) from
     * <code>this.points</code> using the value of that
     * point as the chance for that point to be selected.
     *
     * If not enough points are found in the allowed number
     * of checks, then random points are selected, not taking
     * the point's value into account
     *
     * @param amount Number of points to get
     * @return The found points
     */
    public Set<Point> getLocations(int amount) {
        Set<Point> points = new HashSet<>();
        
        while (points.size() < amount) {
            points.add(this.points.get());
        }

        return points;
    }

    public Point getLocation() {
        return points.get();
    }

    @Override
    public String getKeyword() {
        return "Spread_Image";
    }

    @Override
    public SpreadImage serialize(File file, ConfigurationSection configurationSection, String path) {
        String imageName = configurationSection.getString(path + ".Name", null);

        // Avoid an error. Image name is a required field
        if (imageName == null) {
            return null;
        }

        // Consider the following examples:
        // "circle.jpeg"
        // "circle.png"
        // "circle"
        //
        // Assuming the file has the proper extension, all of
        // the examples should work (Though, only png files are
        // tested). If there is no file extension given (or, in
        // other terms, there is no "." in the file name), add the
        // png file extension
        if (!imageName.contains("\\.")) imageName += ".png";

        int FOVWidth = configurationSection.getInt(path + ".Field_Of_View_Width", 45);
        int FOVHeight = configurationSection.getInt(path + ".Field_Of_View_Height", 45);

        File dataFolder = WeaponMechanics.getPlugin().getDataFolder();
        File sprites = new File(dataFolder, "spread_patterns");

        Sprite sprite = new Sprite(new File(sprites, imageName));
        SpreadImage image = new SpreadImage(sprite, FOVWidth, FOVHeight);

        debug.validate(!image.points.isEmpty(), "The spread image with the name \"" + imageName + "\" has no points!",
                "Found in file " + file + " at path " + path);

        // Helps avoid having multiple if statements, easier to look at this way
        boolean isFailed = !sprites.exists()
                || sprite.getPixels() == null
                || image.points.isEmpty();

        if (isFailed) {
            // Since (Most of the time) the player has
            // already been told about the failure, this
            // doesn't need to be specific
            debug.log(LogLevel.ERROR, "Failed to serialize spread image!");
            return null;
        }

        return image;
    }
}
