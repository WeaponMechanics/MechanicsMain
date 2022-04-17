package me.deecaad.core.commands.wrappers;

import org.bukkit.Location;

public class Rotation {

    private float yaw;
    private float pitch;

    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void apply(Location location) {
        location.setYaw(yaw);
        location.setPitch(pitch);
    }
}
