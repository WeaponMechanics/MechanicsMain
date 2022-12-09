package me.deecaad.core.file.inline;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface InlineSerializer<T> extends Serializer<T> {

    List<Argument>

    @Override
    String getKeyword();

    @NotNull
    @Override
    default T serialize(SerializeData data) throws SerializerException {

    }
}
