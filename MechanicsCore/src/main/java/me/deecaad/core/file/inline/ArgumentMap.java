package me.deecaad.core.file.inline;

import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.SerializerOptionsException;
import me.deecaad.core.file.SerializerTypeException;
import me.deecaad.core.file.inline.types.ListType;
import me.deecaad.core.file.inline.types.NestedType;
import me.deecaad.core.file.inline.types.RegistryType;

import java.util.*;
import java.util.stream.Collectors;

public class ArgumentMap {

    private final Map<String, Argument> args;

    public ArgumentMap(List<Argument> args) {
        this.args = args.stream().collect(Collectors.toMap(arg -> arg.getName().toLowerCase(Locale.ROOT), arg -> arg));
    }

    public ArgumentMap(Argument... args) {
        this.args = Arrays.stream(args).collect(Collectors.toMap(arg -> arg.getName().toLowerCase(Locale.ROOT), arg -> arg));
    }

    public ArgumentMap addAll(Argument... args) {
        this.args.putAll(Arrays.stream(args).collect(Collectors.toMap(arg -> arg.getName().toLowerCase(Locale.ROOT), arg -> arg)));
        return this;
    }

    public Map<String, Argument> getArgs() {
        return args;
    }

    /**
     * Returns <code>true</code> if there are no arguments.
     *
     * @return true if the internal argument map is empty.
     */
    public boolean isEmpty() {
        return args.isEmpty();
    }
}
