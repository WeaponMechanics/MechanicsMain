package temp;

import org.bukkit.util.Vector;

public interface Offsetable {

    Vector getOffset();

    void setOffset(Vector offset);

    default void setOffset(double x, double y, double z) {
        setOffset(new Vector(x, y, z));
    }
}
