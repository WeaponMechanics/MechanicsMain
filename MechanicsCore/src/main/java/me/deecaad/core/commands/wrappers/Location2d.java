package me.deecaad.core.commands.wrappers;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Location2d extends Location {

    public Location2d(@Nullable World world, double x, double z) {
        super(world, x, 0.0, z);
    }

    public Location2d(@Nullable World world, double x, double z, float yaw, float pitch) {
        super(world, x, 0.0, z, yaw, pitch);
    }

    @NotNull
    @Override
    public Block getBlock() {
        throw new Location2Exception();
    }

    @Override
    public void setY(double y) {
        throw new Location2Exception();
    }

    @Override
    public double getY() {
        throw new Location2Exception();
    }

    @Override
    public int getBlockY() {
        throw new Location2Exception();
    }

    public static class Location2Exception extends RuntimeException {
    }
}
