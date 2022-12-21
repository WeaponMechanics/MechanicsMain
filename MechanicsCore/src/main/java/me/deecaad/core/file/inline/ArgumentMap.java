package me.deecaad.core.file.inline;

import me.deecaad.core.file.SerializerOptionsException;
import me.deecaad.core.file.SerializerTypeException;
import me.deecaad.core.file.inline.types.InlineSerializerType;

import java.util.*;
import java.util.stream.Collectors;

public class ArgumentMap {

    private final Map<String, Argument> args;

    public ArgumentMap(List<Argument> args) {
        this.args = args.stream().collect(Collectors.toMap(Argument::getName, arg -> arg));
    }

    public ArgumentMap(Argument... args) {
        this.args = Arrays.stream(args).collect(Collectors.toMap(Argument::getName, arg -> arg));
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

    /**
     * When an inline serializer only has 1 required argument, there is no need
     * to specify the name of the argument. For example, the sound mechanic
     * only requires the 'sound' argument. So, 'sound(ENTITY_GENERIC_EXPLOSION)'
     * is the same as 'sound(sound=ENTITY_GENERIC_EXPLOSION)'
     *
     * @return true if there is only 1 required argument.
     */
    public boolean canUseShorthand() {
        return args.size() == 1 || args.values().stream().filter(Argument::isRequired).count() == 1L;
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
            Argument arg = args.get(key);

            // User input a bad key, no matching argument.
            if (arg == null)
                throw new InlineException(key, new SerializerOptionsException("", "Argument", args.keySet(), key, ""));

            // If there is another element in the stack, that means we have
            // nested inline serializers. In this case, the 'key' *MUST* point
            // to an InlineSerializerType (since it has to be nested).
            if (iterator.hasNext()) {
                if (arg.getType() instanceof InlineSerializerType cast)
                    args = cast.getSerializer().args().args;
                else
                    throw new InlineException(key, key.length() + 1, new SerializerTypeException("", arg.getType().getClass(), Map.class, "{UNKNOWN}", ""));
            }
        }

        return args.get(key);
    }

    public Argument getArgument(LinkedList<String> stack, String key) throws InlineException {
        LinkedList<String> temp = new LinkedList<>(stack);
        temp.push(key);
        return getArgument(temp);
    }
}
