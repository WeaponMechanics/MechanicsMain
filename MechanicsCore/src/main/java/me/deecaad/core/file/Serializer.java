package me.deecaad.core.file;

import me.deecaad.core.utils.LogLevel;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static me.deecaad.core.MechanicsCore.debug;

public interface Serializer<T> {

    /**
     * Returns the unique identifier to this serializer. This identifier is
     * used to determine when to apply the serializer to a config section.
     * The identifier is case-sensitive, and it is ignored when
     * <code>null</code>.
     *
     * <p>Generally speaking, you should always override this method. When you
     * do not need automatic serializer handling, you may return null.
     *
     * @return The nullable unique identifier.
     */
    default @Nullable String getKeyword() {
        return null;
    }

    /**
     * Allows using this serializer under other serializers if the current path ends with
     * any given string in this list.
     *
     * <p> Example values here could be Arrays.asList("Spread.Spread_Image", "Recoil_Pattern").
     * Which would allow this serializer to be used under Spread.Spread_Image and Recoil_Patten parent keywords.
     *
     * @return The nullable parent paths
     */
    default @Nullable List<String> getParentKeywords() {
        return null;
    }

    /**
     * After the {@link #getKeyword()} check and {@link #getParentKeywords()}
     * check, this final check can be customized by the serializer in order to
     * "fine tune" when a serializer should be automatically serialized.
     *
     * @param data The config information.
     * @return true if the serializer should serialize.
     */
    default boolean shouldSerialize(@NotNull SerializeData data) {
        return true;
    }

    /**
     * Basically if this is not null then all other serializers will be used except these which
     * have useLater() returning not null. useLater() should only return something else than null if path to configuration option is used.
     *
     * Path to should not be never used multiple times inside one serializer!
     *
     * @param configurationSection the configuration section
     * @param path the path to this serializer's path (path to keyword like path.keyword)
     * @return true if this serializer should be used later
     */
    default @Nullable String useLater(ConfigurationSection configurationSection, String path) {

        // Checks if keyword is actually an string
        // -> If it is, then it means that it is used as path to other location where
        // this serializer's object should actually be held
        //
        // We have to check if there is a String at that key because
        // ConfigurationSections (for some dumb reason) using .toString
        // instead of type casting
        return configurationSection.isString(path) ? configurationSection.getString(path) : null;
    }

    /**
     * This should be used in new iteration of serialization for all serializers which had Path_To used.
     *
     * @param filledMap the already filled map
     * @param pathWhereToStore the path where the SAME object at filledMap should also be stored (just under different key)
     * @param pathTo the path where to try to find object from filledMap
     */
    default void tryPathTo(Configuration filledMap, String pathWhereToStore, String pathTo) {
        Object obj = filledMap.getObject(pathTo);
        if (!this.getClass().isInstance(obj)) {
            String[] splittedWhereToStore = pathWhereToStore.split("\\.");
            debug.log(LogLevel.ERROR, "Tried to use path to, but didn't find correct object.",
                    "1) Make sure that you wrote path to correctly (" + pathTo + ")",
                    "2) Make sure that the object at path to actually exists.",
                    "3) Make sure that the object at path to doesn't also use path to as this may cause conflicts.",
                    "4) If you feel like you weren't even intending to use path to, make sure that " + splittedWhereToStore[splittedWhereToStore.length - 1] + " doesn't match any serializer keyword!",
                    "This is located at " + pathWhereToStore + " in configurations.");
            return;
        }
        filledMap.set(pathWhereToStore, obj);
    }

    /**
     * Returns a link to the page on the wiki that describes this serializer.
     * This method is called from {@link SerializeData}, and is used to help
     * the user find potential solutions to their problem.
     *
     * @return The nullable link to the wiki.
     */
    @Nullable
    default String getWikiLink() {
        return null;
    }

    /**
     * Returns <code>true</code> if the serializer allows path-to. You should
     * override this method to return <code>false</code> if your serializer
     * accepts a {@link String} in the main path of the serializer. For
     * example, the {@link me.deecaad.core.file.serializers.VectorSerializer}
     * should override this method to return <code>false</code>.
     *
     * @return true if the serializer is complicated enough for path-to.
     */
    default boolean canUsePathTo() {
        return getKeyword() != null;
    }

    /**
     * Returns <code>true</code> when the given key can be "added" to this
     * serializer, and should be saved to the main configuration map. This is
     * useful when using {@link SerializeData#step(Serializer)} with path-to.
     *
     * @param key The non-null key to check
     * @return true if the key should be saved.
     */
    default boolean letPassThrough(@NotNull String key) {
        return false;
    }

    @NotNull
    default String getName() {
        // Sometimes a class will end with 'Serializer' in its name, like
        // 'ColorSerializer'. This information may be confusing to some people,
        // so we can strip it away here.
        String simple = getClass().getSimpleName();
        int index = simple.indexOf("Serializer");
        if (index > 0)
            simple = simple.substring(0, index);
        return simple;
    }

    /**
     * Instantiates a new Object to be added into the finalized configuration.
     * The object should be built off of {@link SerializeData#config}. If there
     * is any misconfiguration (or any other issue preventing the construction
     * of an object), then this method should throw a
     * {@link SerializerException}. This method may not return null.
     *
     * @param data The non-null data containing config
     * @return The non-null serialized data.
     * @throws SerializerException If there is an error in config.
     */
    @NotNull T serialize(@NotNull SerializeData data) throws SerializerException;
}
