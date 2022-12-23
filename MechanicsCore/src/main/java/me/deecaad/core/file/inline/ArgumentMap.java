package me.deecaad.core.file.inline;

import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.SerializerOptionsException;
import me.deecaad.core.file.SerializerTypeException;
import me.deecaad.core.file.inline.types.NestedType;

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

    public Argument getArgument(LinkedList<String> stack) throws InlineException {

        // Should never happen, but will cause errors, so lets make sure.
        if (stack.isEmpty())
            throw new IllegalArgumentException("Stack was empty... did something go wrong? " + stack);

        Map<String, Argument> args = this.args;
        String key = null;

        Iterator<String> iterator = stack.iterator();
        while (iterator.hasNext()) {
            key = iterator.next();
            if (key == null)
                key = getImplied(args);

            Argument arg = args.get(key.toLowerCase(Locale.ROOT));

            // User input a bad key, no matching argument.
            if (arg == null)
                throw new InlineException(key, new SerializerOptionsException("", "Argument", args.keySet(), key, ""));

            // If there is another element in the stack, that means we have
            // nested inline serializers. In this case, the 'key' *MUST* point
            // to an InlineSerializerType (since it has to be nested).
            if (iterator.hasNext()) {
                if (arg.getType() instanceof NestedType cast)
                    args = cast.getSerializer().args().args;
                else
                    throw new InlineException(key, key.length() + 1, new SerializerTypeException("", arg.getType().getClass(), Map.class, "{UNKNOWN}", ""));
            }
        }

        if (key == null)
            key = getImplied(args);

        return args.get(key.toLowerCase(Locale.ROOT));
    }

    public Argument getArgument(LinkedList<String> stack, String key) throws InlineException {
        LinkedList<String> temp = new LinkedList<>(stack);
        temp.addLast(key);
        return getArgument(temp);
    }

    private static String getImplied(Map<String, Argument> args) throws InlineException {
        // When an inline serializer only has 1 required argument, there is
        // no need to specify the name of the argument. For example, the
        // sound mechanic only requires the 'sound' argument. So
        // 'sound(sound=ENTITY_GENERIC_EXPLOSION)' is the same as
        // 'sound(ENTITY_GENERIC_EXPLOSION)'
        if (args.size() != 1 && args.values().stream().filter(Argument::isRequired).count() != 1L)
            throw new InlineException("(", new SerializerException("", new String[]{"Cannot use shorthand"}, ""));

        return args.size() == 1 ? args.keySet().stream().findFirst().get() : args.values().stream().filter(Argument::isRequired).findFirst().get().getName();
    }
}
