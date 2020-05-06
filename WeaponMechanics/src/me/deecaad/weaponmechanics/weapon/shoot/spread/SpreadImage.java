package me.deecaad.weaponmechanics.weapon.shoot.spread;

import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.ArrayUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

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
    
    private List<Point> points;
    private int height, width;
    
    /**
     * Default constructor for serialization
     */
    public SpreadImage() {}
    
    public SpreadImage(Sprite sprite, int fovWidth, int fovHeight) {
        this.points = new ArrayList<>();

        width = sprite.getWidth();
        height = sprite.getHeight();
        
        double xMiddle = width / 2.0;
        double yMiddle = height / 2.0;
        
        int[][] pixels = ArrayUtils.toBlackAndWhite(sprite.getPixels());
        for (int y = 0; y < pixels.length; y++) {
            for (int x = 0; x < pixels[y].length; x++) {
                if (pixels[y][x] != 0) {
                    double xPos = (x - xMiddle) / sprite.getWidth() * fovWidth;
                    double yPos = (y - yMiddle) / sprite.getHeight() * fovHeight;
                    points.add(new Point(xPos, yPos, pixels[y][x]));
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
    
    public Set<Point> getLocations(int amount) {
        return getLocations(amount, amount * 3);
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
     * @param maxChecks The maximum number of checks to find the points
     * @return The found points
     */
    public Set<Point> getLocations(int amount, int maxChecks) {
        Set<Point> points = new HashSet<>();
        ThreadLocalRandom current = ThreadLocalRandom.current();
        
        while (points.size() < amount) {
            Point point = this.points.get(current.nextInt(this.points.size()));
            if (point.getValue() > current.nextInt(255)) {
                points.add(point);
            }

            // If we checked too many times, fill in the
            // rest of the points
            if (maxChecks-- == 0) {
                for (int i = points.size(); i < amount; i++) {
                    points.add(this.points.get(current.nextInt(this.points.size())));
                }
            }
        }
        return points;
    }

    public Point getLocation(int maxChecks) {
        ThreadLocalRandom current = ThreadLocalRandom.current();

        while (0 < maxChecks--) {
            Point point = this.points.get(current.nextInt(this.points.size()));
            if (point.getValue() > current.nextInt(255)) {
                return point;
            }
        }

        // Guaranteed hit
        return this.points.get(current.nextInt(this.points.size()));
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
            debug.log(LogLevel.ERROR, "Name is a required field! Make sure to specify a valid name.");
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
        if (imageName.contains("\\.")) imageName += ".png";

        int FOVWidth = configurationSection.getInt(path + ".Field_Of_View_Width", 90);
        int FOVHeight = configurationSection.getInt(path + ".Field_Of_View_Height", 90);
        
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
