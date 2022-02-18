package me.deecaad.core.file;

import org.junit.jupiter.api.Test;

import java.io.File;

public class CommentConfigurationTest {

    @Test
    void testWriteConfig() {

    }

    @Test
    void testParseConfig() {
        CommentConfiguration config = new CommentConfiguration(getClass().getClassLoader().getResourceAsStream("comment-config.yml"));

        File file = new File(getClass().getClassLoader().getResource("").getPath(), "output.yml");
        config.write(file);
    }
}
