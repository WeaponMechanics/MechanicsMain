package me.deecaad.core.file;

import java.util.Map;

public interface Deserializer<T> {
    
    Map<String, Object> deserialize(String key, T t);
    
}
