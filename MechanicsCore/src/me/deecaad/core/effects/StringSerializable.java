package me.deecaad.core.effects;

import java.util.Map;

/**
 * Incomplete
 *
 * @param <T> The type to serialize
 */
public interface StringSerializable<T> {

    T serialize(Map<String, Object> args);

    Map<String, Object> getDefaults();
}
