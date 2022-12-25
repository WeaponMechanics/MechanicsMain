package me.deecaad.core.file.inline.types;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.inline.ArgumentType;
import me.deecaad.core.file.inline.InlineException;
import me.deecaad.core.file.serializers.ColorSerializer;
import org.bukkit.Color;

public class ColorType implements ArgumentType<Color> {

    public ColorType() {
    }

    @Override
    public Color serialize(String str) throws InlineException {
        try {
            SerializeData DUMMY = new SerializeData((String) null, null, null, null);
            return new ColorSerializer().fromString(DUMMY, str);
        } catch (SerializerException ex) {
            throw new InlineException(str, ex);
        }
    }

    @Override
    public String example() {
        return "#ff00ff";
    }
}
