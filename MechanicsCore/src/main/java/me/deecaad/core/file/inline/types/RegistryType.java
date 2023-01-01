package me.deecaad.core.file.inline.types;

import me.deecaad.core.file.inline.ArgumentType;
import me.deecaad.core.file.inline.InlineException;
import me.deecaad.core.file.inline.InlineSerializer;
import me.deecaad.core.mechanics.Registry;

import java.util.stream.Collectors;

public class RegistryType<T extends InlineSerializer<T>> implements ArgumentType<Registry<T>> {

    private final Registry<T> registry;

    public RegistryType(Registry<T> registry) {
        this.registry = registry;
    }

    public Registry<T> getRegistry() {
        return registry;
    }

    @Override
    public Registry<T> serialize(String str) throws InlineException {
        throw new UnsupportedOperationException("Cannot call serialize(String) on RegistryType");
    }

    @Override
    public String example() {
        String example = registry.getOptions().stream().findAny().orElseThrow();
        return example + "(" + registry.get(example).args().getArgs().values().stream()
                .map(arg -> arg.getName() + "=" + arg.getType().example()).collect(Collectors.joining(", ")) + ")";
    }

    @Override
    public boolean isComplex() {
        return true;
    }
}
