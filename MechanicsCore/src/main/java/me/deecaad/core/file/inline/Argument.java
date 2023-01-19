package me.deecaad.core.file.inline;

import me.deecaad.core.utils.StringUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public final class Argument {

    private final String name;
    private final ArgumentType<?> type;
    private final Object defaultValue;
    private final boolean required;
    private final List<ArgumentValidator> validators;

    public Argument(String name, ArgumentType<?> type, Object defaultValue) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.required = false;
        this.validators = new LinkedList<>();
    }

    public Argument(String name, ArgumentType<?> type) {
        this.name = name;
        this.type = type;
        this.defaultValue = null;
        this.required = true;
        this.validators = new LinkedList<>();
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

    public Argument addValidator(ArgumentValidator validator) {
        validators.add(validator);
        return this;
    }

    public Object serialize(String str) throws InlineException {
        try {
            Object object = type.serialize(str);
            for (ArgumentValidator validator : validators)
                validator.validate(object);
            return object;

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
        // Since only the argument name is hashed, duplicate names are
        // not allowed.
        return name.hashCode();
    }
}
