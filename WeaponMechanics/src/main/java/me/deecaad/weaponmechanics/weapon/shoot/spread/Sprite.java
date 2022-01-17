package me.deecaad.weaponmechanics.weapon.shoot.spread;

import me.deecaad.weaponmechanics.utils.ArrayUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * This class serves to get a 2 dimensional array of
 * integers from a <code>BufferedImage</code>. The integers
 * follow standard rgb format using a byte for each color.
 */
public class Sprite {
    
    private final int[][] pixels;
    private final int height, width;

    public Sprite(File file) throws IOException {
        BufferedImage img = ImageIO.read(file);
        
        height = img.getHeight();
        width = img.getWidth();
        
        // Gets the RGB value of every pixel in
        // the given image.
        pixels = ArrayUtil.split(img.getRGB(0, 0, width, height, null, 0, width), width);
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
