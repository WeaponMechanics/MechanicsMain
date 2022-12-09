package me.deecaad.core.mechanics.inline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Similar to the {@link me.deecaad.core.file.SerializerException}, this
 * exception stores information to help server-admins find invalid input.
 */
public class InlineException extends Exception {

    private int index;
    private List<String> messages;

    public InlineException(int index, String... messages) {
        this.index = index;
        this.messages = new ArrayList<>(Arrays.asList(messages));
    }

    public InlineException addMessage(String... messages) {
        Collections.addAll(this.messages, messages);
        return this;
    }
}
