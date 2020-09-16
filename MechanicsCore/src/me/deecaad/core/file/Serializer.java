package me.deecaad.core.file;

import me.deecaad.core.utils.LogLevel;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;

import static me.deecaad.core.MechanicsCore.debug;

public interface Serializer<T> {

    /**
     * @return keyword of this serializer used in configurations
     */
    String getKeyword();

    /**
     * Basically if this is not null then all other serializers will be used except these ones which
     * have useLater() returning not null. useLater() should only return something else than null if path to configuration option is used.
     *
     * Path to should not be never used multiple times inside one serializer!
     *
     * @param configurationSection the configuration section
     * @param path the path to this serializer's path (path to keyword like path.keyword)
     * @return true if this serializer should be used later
     */
    default String useLater(ConfigurationSection configurationSection, String path) {

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
        Object obj = filledMap.getObject(pathTo, null);
        if (obj == null || !this.getClass().isInstance(obj.getClass())) {
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
     * You should return null if configuration section at this path wasn't valid,
     * otherwise configuration filling will be messed up. After the keyword
     * everything required should be saved into this method's return object because
     * FileAPI will not save anything after this keyword anymore if this doesn't return null.
     *
     * @param file the file being filled
     * @param configurationSection the configuration section
     * @param path the path to this serializer's path (path to keyword like path.keyword)
     * @return the serialized object or null
     */
    T serialize(File file, ConfigurationSection configurationSection, String path);
}
