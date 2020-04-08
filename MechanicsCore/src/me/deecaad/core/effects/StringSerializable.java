package me.deecaad.core.effects;

import java.util.Map;

/**
 * Incomplete
 *
 * @param <T> The type to serialize
 */
public interface StringSerializable<T> {

    T serialize(Map<String, SerializerData<?>> args);

    Map<String, SerializerData<?>> getDefaults();

    class SerializerData<V> {

        private V data;

        public SerializerData(V data) {
            this.data = data;
        }

        public V getData() {
            return data;
        }
    }
}
