package me.deecaad.core.file;

import me.deecaad.core.utils.Debugger;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtil;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class SerializerException extends Exception {

    private String serializerName;
    private String[] messages;
    private String location;

    public SerializerException(String serializerName, String[] messages, String location) {
        this.serializerName = serializerName;
        this.messages = messages;
        this.location = location;
    }

    /**
     * @param serializer The serializer that generated the exception.
     * @param messages   The messages telling the user the error and how to fix it.
     * @param location   The file + path location to the issue {@link StringUtil#foundAt(File, String)}
     */
    public SerializerException(@Nonnull Serializer<?> serializer, String[] messages, @Nonnull String location) {
        this.messages = messages;
        this.location = location;

        // Sometimes a class will end with 'Serializer' in its name, like
        // 'ColorSerializer'. This information may be confusing to some people,
        // so we can strip it away here.
        String simple = serializer.getClass().getSimpleName();
        int index = simple.indexOf("Serializer");
        if (index > 0)
            simple = simple.substring(0, index);

        serializerName = simple;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void log(Debugger debug) {
        LinkedList<String> collected = new LinkedList<>();

        collected.add("A mistake was found in your configurations when making '" + serializerName + "'");
        collected.addAll(Arrays.asList(messages));
        collected.add(location);
        collected.add(""); // Add an empty string for blank line between errors

        debug.log(LogLevel.ERROR, collected.toArray(new String[0]));
    }

    public SerializerException addMessage(String message) {
        String[] copy = new String[messages.length + 1];
        System.arraycopy(messages, 0, copy, 0, messages.length);
        copy[messages.length] = message;
        this.messages = copy;

        return this;
    }


    public static String forValue(Object value) {
        return "Found value: " + value;
    }

    public static <T extends Enum<T>> String didYouMean(String input, Class<T> enumClass) {
        return didYouMean(input, EnumUtil.getOptions(enumClass));
    }

    public static String didYouMean(String input, Iterable<String> options) {
        String expected = StringUtil.didYouMean(input, options);
        return "Did you mean to use '" + expected + "' instead of '" + input + "'?";
    }

    public static String examples(String... examples) {
        StringBuilder builder = new StringBuilder("'");

        for (String str : examples)
            builder.append(str).append("', ");

        builder.setLength(builder.length() - 2);

        return "Example values: " + builder;
    }

    public static String possibleValues(Iterable<String> options, int count) {

        // We want to know how many elements are in the iterable. We don't care
        // about performance. We need this to tell the user how many options there
        // actually are. This is important since we may only show 5 or 6 options,
        // but it is important for the USER to know that there are more than 5 or 6.
        int i = 0;
        for (String ignore : options) {
            i++;
        }

        count = Math.min(i, count);
        StringBuilder builder = new StringBuilder("Showing ");
        Iterator<String> iterator = options.iterator();

        // Writes either 'All' or a fraction like '4/32'
        if (count == i)
            builder.append("All");
        else
            builder.append(count).append('/').append(i);

        builder.append(" Options:");

        // Append some possible values
        while (iterator.hasNext() && count-- > 0) {
            String next = iterator.next();
            builder.append(" '").append(next).append("'");
        }

        return builder.toString();
    }
}
