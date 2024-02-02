package me.deecaad.core.commands.wrappers;

import org.bukkit.World;

import java.util.Objects;

public class Column {

    private World world;
    private int x;
    private int z;

    public Column(World world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Column column = (Column) o;
        return x == column.x && z == column.z && Objects.equals(world, column.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, z);
    }
}
