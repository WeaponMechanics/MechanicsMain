package me.deecaad.weaponmechanics.weapon.shoot.spread;

import me.deecaad.core.utils.DebugUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.utils.ArrayUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This class serves to get a 2 dimensional array of
 * integers from a <code>BufferedImage</code>. The integers
 * follow standard rgb format using a byte for each color.
 */
public class Sprite {
    
    private int[][] pixels;
    private int height, width;

    public Sprite(File file) {

        // Attempts to get an image from a file.
        // The most common mistake would be an
        // invalid path.
        // This works with .png files. It may
        // not work with other files.
        BufferedImage img = null;
        try {
            img = ImageIO.read(file);
        } catch (IOException e) {

            // Gets all file names, double checks to make sure they are png files,
            // removes the .png, then joins all the remaining strings together
            String files = "\"" + Arrays.stream(file.getParentFile().listFiles())
                    .map(File::getName)
                    .filter(name -> name.endsWith(".png"))
                    .map(str -> str.substring(0, str.length() - ".png".length()))
                    .collect(Collectors.joining("\", \"")) + "\"";
            
            DebugUtil.log(LogLevel.ERROR,
                    "Failed to load image " + file.getName() + " at " + file.getPath(),
                    "You may have put in an incorrect image name",
                    "Valid names: " + files);
            return;
        }
        
        height = img.getHeight();
        width = img.getWidth();
        
        // Gets the RGB value of every pixel in
        // the given image.
        pixels = ArrayUtils.split(img.getRGB(0, 0, width, height, null, 0, width), width);
    }
    
    public int[][] getPixels() {
        return pixels;
    }
    
    public int getHeight() {
        return height;
    }
    
    public int getWidth() {
        return width;
    }
}
