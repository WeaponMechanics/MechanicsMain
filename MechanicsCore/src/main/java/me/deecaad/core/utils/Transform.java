package me.deecaad.core.utils;

import org.bukkit.util.Vector;

import java.util.List;

public class Transform {

    private Vector localPosition;
    private Quaternion localRotation;

    // Transforms inherit their parent's position/rotation
    private Transform parent;
    private List<Transform> children;


    public Transform() {
    }

    public Transform(Transform parent) {
        setParent(parent);
    }

    public Transform getParent() {
        return parent;
    }

    public void setParent(Transform parent) {
        // First we need to adjust based on the previous parent.
        if (this.parent != null) {
            Vector position = this.parent.getPosition();
            localPosition.add(position);
            localRotation = getRotation();

            this.parent.children.remove(this);
        }

        // Now we need to adjust it for the new parent's position
        if (parent != null) {
            Vector position = parent.getPosition();
            localPosition.subtract(position);

            // Bit more complicated... Find the difference between the 2
            // quaternions
            Quaternion parentRotation = parent.getRotation();
            localRotation = parentRotation.inverse().multiply(localRotation);

            parent.children.add(this);
        }

        this.parent = parent;
    }

    public Vector getForward() {
        return getRotation().multiply(Quaternion.FORWARD);
    }

    public Vector getRight() {
        return getRotation().multiply(Quaternion.RIGHT);
    }

    public Vector getUp() {
        return getRotation().multiply(Quaternion.UP);
    }

    public Vector getLocalPosition() {
        return localPosition.clone();
    }

    public void setLocalPosition(Vector localPosition) {
        this.localPosition = localPosition;
    }

    public Vector getPosition() {
        if (parent == null)
            return getLocalPosition();

        return parent.getPosition().add(parent.getRotation().multiply(localPosition));
    }

    public void setPosition(Vector position) {
        if (parent == null) {
            localPosition = position;
        } else {
            localPosition = parent.getPosition().subtract(position);
        }
    }

    public Quaternion getLocalRotation() {
        return localRotation.clone();
    }

    public void setLocalRotation(Quaternion localRotation) {
        this.localRotation = localRotation;
    }

    public Quaternion getRotation() {
        if (parent == null)
            return getLocalRotation();

        return parent.getRotation().multiply(localRotation);
    }
}
