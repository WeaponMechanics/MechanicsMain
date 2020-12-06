package me.deecaad.core.packetlistener;

import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class Packet {

    private Player player;
    private Object packet;
    private boolean cancelled;

    public Packet(Player player, Object packet) {
        this.player = player;
        this.packet = packet;
    }

    /**
     * @return the player for who packet is being sent
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * @return the packet instance
     */
    public Object getPacket() {
        return this.packet;
    }

    /**
     * Checks if packet sending is cancelled
     *
     * @return the state of cancelling
     */
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Sets packet cancellation state.
     * False means that this packet wont be sent to the player.
     *
     * @param cancelled the new cancelling state
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Gets field from some specific super class of this instance's packet instance
     *
     * @param fieldName the field name to search
     * @param superClass the amount of super class
     * @return the field from the super class
     */
    public Field getField(String fieldName, int superClass) {
        Class<?> packetClass = this.packet.getClass();
        if (superClass > 0) {
            int i = 0;
            while (i < superClass) {
                packetClass = packetClass.getSuperclass();
                ++i;
            }
        }
        return ReflectionUtil.getField(packetClass, fieldName);
    }

    /**
     * Gets value from some field from this instance's packet instance
     *
     * @param fieldName the field name to search
     * @return the object of field from the super class
     */
    public Object getFieldValue(String fieldName) {
        return getFieldValue(fieldName, 0);
    }

    /**
     * Gets value from some field from specific super class of this instance's packet instance
     *
     * @param fieldName the field name to search
     * @param superClass the amount of super class
     * @return the object of field from the super class
     */
    public Object getFieldValue(String fieldName, int superClass) {
        return ReflectionUtil.invokeField(getField(fieldName, superClass), this.packet);
    }

    /**
     * Sets new value for field in some specific super class
     *
     * @param fieldName the field name to change
     * @param value the new value for the field
     * @param superClass the amount of super class
     */
    public void setFieldValue(String fieldName, Object value, int superClass) {
        ReflectionUtil.setField(getField(fieldName, superClass), this.packet, value);
    }

    /**
     * Gets value from some field of this instance's packet instance
     *
     * @param field the field invoked
     * @return the object of field
     */
    public Object getFieldValue(Field field) {
        return ReflectionUtil.invokeField(field, this.packet);
    }

    /**
     * Sets new value for field
     *
     * @param field the field invoked
     * @param value the new value for the field
     */
    public void setFieldValue(Field field, Object value) {
        ReflectionUtil.setField(field, this.packet, value);
    }
}