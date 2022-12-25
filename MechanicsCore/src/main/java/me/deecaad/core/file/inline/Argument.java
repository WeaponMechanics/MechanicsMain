package me.deecaad.core.file.inline;

import me.deecaad.core.utils.StringUtil;

public final class Argument {

    private final String name;
    private final ArgumentType<?> type;
    private final Object defaultValue;
    private final boolean required;

    public Argument(String name, ArgumentType<?> type, Object defaultValue) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.required = false;
    }

    public Argument(String name, ArgumentType<?> type) {
        this.name = name;
        this.type = type;
        this.defaultValue = null;
        this.required = true;
    }

    public String getName() {
        return name;
    }

    public String getAsYamlKey() {
        return StringUtil.upperSnakeCase(StringUtil.camelToSnake(name));
    }

    public ArgumentType<?> getType() {
        return type;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public boolean isRequired() {
        return required;
    }

    public Object serialize(String str) throws InlineException {
        try {
            return type.serialize(str);
        } catch (InlineException ex) {
            ex.setLookAfter(name);
            throw ex;
        }
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
