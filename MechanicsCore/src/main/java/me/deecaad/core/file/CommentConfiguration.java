package me.deecaad.core.file;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import me.deecaad.core.utils.StringUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CommentConfiguration {

    public static final Pattern INLINE_COMMENT = Pattern.compile("");

    /**
     * Header comments are written at the top of the file.
     */
    private final List<String> header;

    /**
     * Stores comments that go before the configuration line. The string
     * argument is the configuration key. The list of strings contains the
     * comments that are inserted before the configuration key. Consider
     * the following YAML code, notice the empty line between 'Some_Other_Key'
     * and the comments.
     *
     * <blockquote><pre>{@code
     *     Some_Other_Key: 'Value'
     *
     *     # Comment line index 0
     *     # Comment line index 1
     *     The_Stored_Key: 'Value'
     * }</pre></blockquote>
     */
    private final Map<String, List<String>> beforeComments;

    /**
     * Stores 1 single comment that goes after the key, but on the same line as
     * that key.
     *
     * <blockquote><pre>{@code
     *     Key: 'Value' # Comment
     * }</pre></blockquote>
     */
    private final Map<String, String> inlineComments;

    /**
     * Stores the actual config key-value pairs.
     */
    private final Map<String, Object> config;


    public CommentConfiguration(InputStream stream) {
        if (stream == null)
            throw new IllegalCommentException("null stream");

        this.header = new ArrayList<>();
        this.beforeComments = new HashMap<>();
        this.inlineComments = new HashMap<>();
        this.config = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {

            String previousLine = null;
            String line;
            boolean isHeader = true;
            List<String> cache = new ArrayList<>();
            String key = "";
            int indent = 0;
            while ((line = reader.readLine()) != null) {
                String label = previousLine == null ? null : previousLine.trim();
                int indexOfColon = label == null ? -1 : label.indexOf(':');
                if (indexOfColon != -1)
                    label = label.substring(0, indexOfColon);

                if (!line.isEmpty()) {
                    int white = 0;
                    while (line.charAt(white) == ' ') {
                        white++;
                    }

                    if (indent > white) {
                        int last = key.lastIndexOf('.');
                        key = key.substring(0, last);
                    } else if (indent < white) {
                        key = key + "." + label;
                    }

                    indent = white;
                }

                previousLine = line;

                // Add some whitespace at the end of the line, so we can skip
                // string length checks later.
                line += " ";

                if (line.trim().startsWith("#")) {
                    String comment = line.substring(1);
                    cache.add(comment);
                    continue;
                }

                // Only a header may have comments followed by an empty line.
                if (line.trim().isEmpty() && !cache.isEmpty()) {
                    if (isHeader) {
                        header.addAll(cache);
                        cache.clear();
                        continue;
                    } else
                        throw new IllegalCommentException("Trailing comments found: " + cache);
                }

                // Headers can only go at the top lines of the file.
                isHeader = false;

                // Now we need to trace from the beginning of the line until we
                // find a non-escaped comment.
                int commentIndex = -1;
                char escape = ' ';
                int start = line.indexOf(':');
                if (start != -1 && !line.trim().startsWith("-")) {
                    for (int i = start; i < line.length(); i++) {
                        if (line.charAt(i) == '#' && escape == ' ') {
                            commentIndex = i;
                            break;
                        } else if (line.charAt(i) == '\'' || line.charAt(i) == '\"') {
                            char previous = escape;
                            escape = line.charAt(i);

                            // Double '' or "" escapes the character.
                            if (line.charAt(i + 1) == escape) {
                                escape = previous;
                                i++;
                            }

                            // If we were already escaped, then we should exit
                            // the escape.
                            if (escape == previous) {
                                escape = ' ';
                            }
                        }
                    }
                }

                String label2 = line.trim();
                int indexOfColon2 = label2.indexOf(':');
                if (indexOfColon2 != -1)
                    label2 = label2.substring(0, indexOfColon2);

                if (!cache.isEmpty())
                    beforeComments.put(key + "." + label2, new ArrayList<>(cache));
                if (commentIndex != -1)
                    inlineComments.put(key + "." + label2, line.substring(commentIndex));

                cache.clear();
            }
            config.putAll(YamlConfiguration.loadConfiguration(new BufferedReader(new InputStreamReader(stream))).getValues(true));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void enforceDefaults(File file) {
        if (!file.exists()) {
            this.write(file);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : this.config.keySet()) {
            int indents = StringUtil.countChars('.', key);

        }

    }

    public void write(File file) {
        StringBuilder builder = new StringBuilder();

        for (String comment : header) {
            builder.append('#').append(comment).append('\n');
        }

        // Create a new line between header and config
        if (builder.length() != 0)
            builder.append('\n');

        config.forEach((key, value) -> {
            int indents = StringUtil.countChars('.', key);
            for (String comment : beforeComments.get(key)) {
                builder.append(StringUtil.repeat("  ", indents)).append('#').append(comment).append('\n');
            }
            builder.append(StringUtil.repeat("  ", indents)).append(key).append(": ").append(value);

            if (inlineComments.containsKey(key)) {
                builder.append(" #").append(inlineComments.get(key));
            }

            builder.append('\n');
        });

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
            Files.createParentDirs(file);
            String data = builder.toString();
            writer.write(data);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    public static class IllegalCommentException extends RuntimeException {
        public IllegalCommentException(String message) {
            super(message);
        }
    }
}
