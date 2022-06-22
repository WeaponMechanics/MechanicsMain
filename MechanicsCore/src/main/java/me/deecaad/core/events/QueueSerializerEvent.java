package me.deecaad.core.events;

import me.deecaad.core.file.Serializer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class QueueSerializerEvent extends Event {

    public static final HandlerList HANDLER_LIST = new HandlerList();

    private JavaPlugin source;
    private File file;
    private List<Serializer<?>> serializers;

    public QueueSerializerEvent(JavaPlugin source, File file) {
        this.source = source;
        this.file = file;
        this.serializers = new ArrayList<>();
    }

    public JavaPlugin getSource() {
        return source;
    }

    public String getSourceName() {
        return source.getName();
    }

    public File getFile() {
        return file;
    }

    public List<Serializer<?>> getSerializers() {
        return serializers;
    }

    public void addSerializers(Serializer<?>... serializers) {
        this.serializers.addAll(Arrays.asList(serializers));
    }

    public void addSerializers(Collection<Serializer<?>> serializers) {
        this.serializers.addAll(serializers);
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
