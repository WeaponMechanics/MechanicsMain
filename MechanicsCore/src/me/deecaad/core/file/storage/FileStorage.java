package me.deecaad.core.file.storage;

import me.deecaad.core.utils.LogLevel;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static me.deecaad.core.MechanicsCore.debug;

public abstract class FileStorage {

    protected final File file;

    protected FileStorage(File file) {

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                debug.log(LogLevel.ERROR, e);
            }
        }

        this.file = file;
    }

    public abstract void write(String path, Map<String, Object> map);

    public abstract Map<String, Object> read(String path);
}
