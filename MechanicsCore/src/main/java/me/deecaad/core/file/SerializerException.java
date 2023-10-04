package me.deecaad.core.file;

import me.deecaad.core.utils.Debugger;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
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
    public SerializerException(@NotNull Serializer<?> serializer, String[] messages, @NotNull String location) {
        this.messages = messages;
        this.location = location;
        this.serializerName = serializer.getName();
    }

    public String getSerializerName() {
        return serializerName;
    }

    public void setSerializerName(String serializerName) {
        this.serializerName = serializerName;
    }

    public String[] getMessages() {
        return messages.clone();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void log(Debugger debug) {
        log(debug, LogLevel.ERROR);
    }

    public void log(Debugger debug, LogLevel level) {
        LinkedList<String> collected = new LinkedList<>();

        collected.add("A mistake was found in your configurations when making '" + serializerName + "'");
        collected.addAll(Arrays.asList(messages));
        collected.add(location);
        collected.add(""); // Add an empty string for blank line between errors

        debug.log(level, collected.toArray(new String[0]));
    }

    public SerializerException addMessage(boolean condition, String message) {
        if (condition)
            addMessage(message);

        return this;
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

    public static String possibleValues(Iterable<String> options, String actual, int count) {

        // We want to know how many elements are in the iterable. We don't care
        // about performance. We need this to tell the user how many options there
        // actually are. This is important since we may only show 5 or 6 options,
        // but it is important for the USER to know that there are more than 5 or 6.
        ArrayList<String> arr = new ArrayList<>();
        options.forEach(arr::add);
        int[] table = StringUtil.mapToCharTable(actual);

        // Sort the list based on what is most similar to 'actual'.
        arr.sort((a, b) -> {
            int[] localA = StringUtil.mapToCharTable(a);
            int[] localB = StringUtil.mapToCharTable(b);

            int differenceA = Math.abs(actual.length() - a.length());
            int differenceB = Math.abs(actual.length() - b.length());

            for (int i = 0; i < table.length; i++) {
                differenceA += Math.abs(table[i] - localA[i]);
                differenceB += Math.abs(table[i] - localB[i]);
            }

            return Integer.compare(differenceA, differenceB);
        });

        count = Math.min(arr.size(), count);
        StringBuilder builder = new StringBuilder("Showing ");

        // Writes either 'All' or a fraction like '4/32'
        if (count == arr.size())
            builder.append("All");
        else
            builder.append(count).append('/').append(arr.size());

        builder.append(" Options:");

        // Append some possible values
        Iterator<String> iterator = arr.iterator();
        while (iterator.hasNext() && count-- > 0) {
            String next = iterator.next();
            builder.append(" '").append(next).append("'");
        }

        return builder.toString();
    }
}
