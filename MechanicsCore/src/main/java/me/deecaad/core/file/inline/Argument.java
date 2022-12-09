package me.deecaad.core.file.inline;

import java.util.List;

public final class Argument {

    private final String name;
    private final List<String> aliases;
    private final ArgumentType<?> type;
    private final Object defaultValue;

    public Argument(String name, ArgumentType<?> type, Object defaultValue) {
        this.name = name;
        this.aliases = List.of();
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public Argument(String name, ArgumentType<?> type) {
        this.name = name;
        this.aliases = List.of();
        this.type = type;
        this.defaultValue = null;
    }

    public Argument(String name, ArgumentType<?> type, Object defaultValue, List<String> aliases) {
        this.name = name;
        this.aliases = aliases;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public Argument(String name, ArgumentType<?> type, List<String> aliases) {
        this.name = name;
        this.aliases = aliases;
        this.type = type;
        this.defaultValue = null;
    }

    public String getName() {
        return name;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public ArgumentType<?> getType() {
        return type;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public boolean matches(String key) {
        return name.equals(key) || aliases.contains(key);
    }

    public boolean isRequired() {
        return defaultValue == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Argument argument = (Argument) o;
        return name.equals(argument.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
