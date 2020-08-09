package me.deecaad.core.mechanics.serialization.datatypes;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.mechanics.serialization.Argument;
import me.deecaad.core.mechanics.serialization.MechanicListSerializer;
import me.deecaad.core.mechanics.serialization.StringSerializable;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtils;
/*import org.intellij.lang.annotations.Language;*/

import java.lang.reflect.Modifier;
import java.util.Map;

import static me.deecaad.core.MechanicsCore.debug;

public class SerializerType<T extends StringSerializable<T>> extends DataType<T> {

    private final Class<T> clazz;
    /*@Language("RegExp")*/ private String nameMatcher;

    public SerializerType(Class<T> clazz, String name) {
        super(name);

        this.clazz = clazz;
        this.nameMatcher = "[^( ]+";
    }

    public String getNameMatcher() {
        return nameMatcher;
    }

    public void setNameMatcher(/*@Language("RegExp")*/ String nameMatcher) {
        this.nameMatcher = nameMatcher;
    }

    @Override
    public T serialize(String str) {

        if (Modifier.isAbstract(clazz.getModifiers())) {

            String inputName = StringUtils.match(nameMatcher, str);

            // Get all subclasses of the string serializable
            // determine which class to serialize
            // serialize it, then return
            for (Class<StringSerializable> serializable : MechanicsCore.getPlugin().getStringSerializers().values()) {

                // Check if the class is a subclass of this serializer's class
                if (serializable.isAssignableFrom(clazz)) {

                    String name = StringSerializable.parseName(serializable);
                    if (name.equals(inputName)) {

                        Argument[] args = StringSerializable.parseArgs(serializable);
                        Map<String, Object> data = MechanicListSerializer.getArguments(name, str, args);
                        return ReflectionUtil.newInstance(clazz).serialize(data);
                    }
                }
            }

            debug.error("Unknown serializer: " + nameMatcher, "Perhaps you spelled it wrong?");
            return null;
        } else {

            String name = StringSerializable.parseName(clazz);
            Argument[] args = StringSerializable.parseArgs(clazz);
            Map<String, Object> data = MechanicListSerializer.getArguments(name, str, args);
            return ReflectionUtil.newInstance(clazz).serialize(data);
        }
    }

    @Override
    public boolean validate(String str) {

        // It's a bit challenging to validate this, will work on that later
        return true;
    }
}
