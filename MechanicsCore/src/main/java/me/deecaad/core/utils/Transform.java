package me.deecaad.core.utils;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Transform {

    private Vector localPosition;
    private Quaternion localRotation;

    // Transforms inherit their parent's position/rotation
    private Transform parent;
    private final List<Transform> children;

    public Transform() {
        children = new ArrayList<>();
        localRotation = Quaternion.identity();
        localPosition = new Vector();
    }

    public Transform(Transform parent) {
        this();
        setParent(parent);
    }

    public Transform getParent() {
        return parent;
    }

    public Transform getChild(int i) {
        return children.get(i);
    }

    public void setParent(Transform parent) {
        // First we need to adjust based on the previous parent.
        if (this.parent != null) {
            localPosition = getPosition();
            localRotation = getRotation();

            this.parent.children.remove(this);
        }

        // Now we need to adjust it for the new parent's position
        if (parent != null) {

            // Bit more complicated... Find the difference between the 2
            // quaternions
            Quaternion parentRotation = parent.getRotation();
            localRotation = parentRotation.inverse().multiply(localRotation);

            Vector position = parent.getPosition();
            localPosition.subtract(position);

            parent.children.add(this);
        }

        this.parent = parent;
    }

    public Vector getForward() {
        return getRotation().multiply(Quaternion.FORWARD);
    }

    public void setForward(Vector forward) {
        setRotation(Quaternion.lookAt(forward, Quaternion.UP));
    }

    public Vector getRight() {
        return getRotation().multiply(Quaternion.RIGHT);
    }

    public void setRight(Vector right) {
        setRotation(Quaternion.fromTo(Quaternion.RIGHT, right));
    }

    public Vector getUp() {
        return getRotation().multiply(Quaternion.UP);
    }

    public void setUp(Vector up) {
        setRotation(Quaternion.fromTo(Quaternion.UP, up));
    }

    public Vector getLocalPosition() {
        return localPosition.clone();
    }

    public void setLocalPosition(Vector localPosition) {
        this.localPosition = localPosition;
    }

    public Vector getPosition() {
        if (getParent() == null)
            return getLocalPosition();

        return getParent().getPosition().add(getParent().getRotation().multiply(localPosition));
    }

    public void setPosition(Location position) {
        setPosition(position.toVector());
    }

    public void setPosition(Vector position) {
        if (getParent() == null) {
            setLocalPosition(position);
        } else {
            Vector parentPos = getParent().getPosition();
            Quaternion parentRot = getParent().getRotation();

            setLocalPosition(parentRot.multiply(position.subtract(parentPos)));
        }
    }

    public Quaternion getLocalRotation() {
        return localRotation.clone();
    }

    public void setLocalRotation(Quaternion localRotation) {
        this.localRotation = localRotation.normalize();
    }

    public Quaternion getRotation() {
        if (getParent() == null)
            return getLocalRotation();

        return getParent().getRotation().multiply(localRotation);
    }

    public void setRotation(Quaternion rotation) {
        if (getParent() == null) {
            setLocalRotation(rotation.normalize());
        } else {
            setLocalRotation(getParent().getRotation().inverse().multiply(rotation));
        }
    }

    public void applyRotation(Quaternion rotation) {
        localRotation.multiply(rotation.normalize());
    }

    public void debug(World world) {
        Vector origin = getPosition();

        debugRay(world, origin, getForward(), Color.BLUE);
        debugRay(world, origin, getUp(), Color.GREEN);
        debugRay(world, origin, getRight(), Color.RED);
    }

    public void debugRay(World world, Vector origin, Vector direction, Color color) {
        Particle.DustOptions options = new Particle.DustOptions(color, 0.3f);
        int count = 10;
        for (int i = 0; i < count; i++) {
            double t = (double) i / count;
            Vector pos = direction.clone().multiply(t).add(origin);
            world.spawnParticle(Particle.REDSTONE, pos.getX(), pos.getY(), pos.getZ(), 1, options);
        }
    }
}
