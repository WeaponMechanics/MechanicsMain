package me.deecaad.core.events;

import me.deecaad.core.file.IValidator;
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

/**
 * This method should be called before a {@link me.deecaad.core.file.FileReader}
 * is instantiated by a plugin using MechanicsCore. This event allows us to
 * register our own {@link Serializer}s and {@link IValidator}s to be used
 * during data serialization.
 *
 * <p>Additionally, MechanicsCore listens for this method to add "common"
 * serializers. At the time of writing (30 July 2022), the following
 * serializers are added by MechanicsCore:
 *
 * <ul>
 *     <li>{@link me.deecaad.core.file.serializers.ItemSerializer}</li>
 *     <li>{@link me.deecaad.core.file.serializers.ChanceSerializer}</li>
 *     <li>{@link me.deecaad.core.file.serializers.ColorSerializer}</li>
 * </ul>
 *
 * <p>If you want to add, for example, the {@link me.deecaad.core.file.serializers.ItemSerializer}
 * <b>WITHOUT</b> the {@link me.deecaad.core.file.serializers.ColorSerializer},
 * then you will have to filter out the added serializers <b>AFTER</b> calling
 * this event.
 */
public class QueueSerializerEvent extends Event {

    public static final HandlerList HANDLER_LIST = new HandlerList();

    private final JavaPlugin source;
    private final File file;
    private final List<Serializer<?>> serializers;
    private final List<IValidator> validators;

    public QueueSerializerEvent(JavaPlugin source, File file) {
        this.source = source;
        this.file = file;
        this.serializers = new ArrayList<>();
        this.validators = new ArrayList<>();
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

    public List<IValidator> getValidators() {
        return validators;
    }

    public void addSerializers(Serializer<?>... serializers) {
        this.serializers.addAll(Arrays.asList(serializers));
    }

    public void addSerializers(Collection<Serializer<?>> serializers) {
        this.serializers.addAll(serializers);
    }

    public void addValidators(IValidator... validators) {
        this.validators.addAll(Arrays.asList(validators));
    }

    public void addValidators(Collection<IValidator> validators) {
        this.validators.addAll(validators);
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
