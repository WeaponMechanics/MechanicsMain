package me.deecaad.weaponmechanics.weapon.firearm;

import java.util.HashMap;
import java.util.Map;

/**
 * This class highlights the different
 * types a Firearm can have.
 *
 * So there are 2 different <code>FirearmAction</code>s,
 * OPEN and CLOSE. These actions are triggered based on
 * weapon shooting and reloading. Comments on each type
 * describe when the firearm opens and closes based on
 * those 2 triggers.
 *
 * Some types also have aliases, <code>REVOLVER</code>
 * and <code>BREAK</code>, for example. This is because
 * these types are called different things by different
 * people.
 *
 * @author cjcrafter
 * @see FirearmAction
 * @since 1.0
 */
public enum FirearmType {
    
    /**
     * FirearmType for revolvers.
     * <pre>
     *   Shoot: Bang
     *   Reload: Open, then reload timer, then close
     *   Open: Empty shells fall out
     *   Close: The block and spin of barrel
     * </pre>
     */
    REVOLVER,
    
    /**
     * FirearmType for pump shotguns
     * <pre>
     *   Shoot: bang, then open, then close
     *   Reload: reload timer, then open, then close
     *   Open: Pull the pump
     *   Close: Push the pump
     * </pre>
     */
    PUMP,
    
    /**
     * FirearmType for bolt action and lever action rifles
     *   Shoot: Bang, then open, (bullet goes in), then close
     *   Reload: Open, reload timer, then close
     *   Open: Pull lever back
     *   Close: Push lever forward
     */
    LEVER;
    
    private static final Map<String, FirearmType> BY_NAME = new HashMap<>();
    
    static {
        for (FirearmType type: values()) {
            BY_NAME.put(type.name(), type);
        }
    }
    
    /**
     * A version of the <code>Enum#valueOf(String)</code> method that
     * does not throw an exception when an invalid enum is input
     *
     * @param name The name of the enum
     * @return The FirearmType from the name
     */
    public static FirearmType getType(String name) {
        return BY_NAME.get(name.toUpperCase());
    }
}
