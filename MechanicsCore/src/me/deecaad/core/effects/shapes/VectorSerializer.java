package me.deecaad.core.effects.shapes;

import me.deecaad.core.utils.DebugUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.VectorUtils;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class VectorSerializer {

    public static void main(String[] args) {
        String arg = "tekt-hi";
        String[] split = arg.split("-");

        String dir = "";
        double length = 1.0;
        try {
            if (split.length != 2) {
                dir = arg;
            } else {
                dir = split[0];
                length = Double.parseDouble(split[1]);
            }

            BlockFace.valueOf(dir).getDirection();
        } catch (NumberFormatException ex) {
            DebugUtil.log(LogLevel.ERROR, split[1] + " is not a number!");
        } catch (IllegalArgumentException ex) {
            DebugUtil.log(LogLevel.ERROR, dir + " is not a valid direction!");
        }

        Vector vector = VectorUtils.setLength(BlockFace.valueOf(dir).getDirection(), length);
        System.out.println(vector);
    }
}
