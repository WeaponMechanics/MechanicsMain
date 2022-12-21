package me.deecaad.core.file.inline;

public interface ArgumentType<T> {
    T serialize(String str) throws InlineException;
}
