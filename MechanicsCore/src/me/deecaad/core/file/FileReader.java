package me.deecaad.core.file;

import me.deecaad.core.utils.DebugUtil;
import me.deecaad.core.utils.LogLevel;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

public class FileReader {

    private final List<PathToSerializer> pathToSerializers;
    private final Map<String, Serializer<?>> serializers;
    private final List<ValidatorData> validatorDatas;
    private final Map<String, IValidator> validators;

    public FileReader(@Nullable List<Serializer<?>> serializers, @Nullable List<IValidator> validators) {
        this.serializers = new HashMap<>();
        this.validators = new HashMap<>();
        this.pathToSerializers = new ArrayList<>();
        this.validatorDatas = new ArrayList<>();
        addSerializers(serializers);
        addValidators(validators);
    }

    /**
     * Adds new serializers for this file reader.
     * Serializers should be added before using fillAllFiles() or fillOneFile() methods.
     *
     * @param serializers the new list serializers for this file reader
     */
    public void addSerializers(List<Serializer<?>> serializers) {
        if (serializers != null && serializers.size() > 0) {
            for (Serializer<?> serializer : serializers) {
                addSerializer(serializer);
            }
        }
    }

    /**
     * Adds new serializer for this file reader.
     * Serializers should be added before using fillAllFiles() or fillOneFile() methods.
     *
     * @param serializer the new serializer for this file reader
     */
    public void addSerializer(Serializer<?> serializer) {
        if (this.serializers.containsKey(serializer.getKeyword().toLowerCase())) {
            DebugUtil.log(LogLevel.ERROR,
                    "Can't add serializer with keyword of " + serializer.getKeyword() + " because some other serializer already has same keyword.");
            return;
        }
        this.serializers.put(serializer.getKeyword().toLowerCase(), serializer);
    }

    /**
     * Adds new validators for this file reader.
     * Validators should be added before using fillAllFiles() or fillOneFile() methods.
     *
     * @param validators the new list validators for this file reader
     */
    public void addValidators(List<IValidator> validators) {
        if (validators != null && validators.size() > 0) {
            for (IValidator validator : validators) {
                addValidator(validator);
            }
        }
    }

    /**
     * Adds new validator for this file reader.
     * Validators should be added before using fillAllFiles() or fillOneFile() methods.
     *
     * @param validator the new validator for this file reader
     */
    public void addValidator(IValidator validator) {
        if (this.validators.containsKey(validator.getKeyword().toLowerCase())) {
            DebugUtil.log(LogLevel.ERROR,
                    "Can't add validator with keyword of " + validator.getKeyword() + " because some other validator already has same keyword.");
            return;
        }
        this.validators.put(validator.getKeyword().toLowerCase(), validator);
    }

    /**
     * Iterates through all .yml files inside every directory starting from the given directory.
     * It is recommended to give this method plugin's data folder directory.
     * This also takes in account given serializers.
     *
     * @param directory the directory
     * @param ignoreFiles ignored files which name starts with any given string
     * @return the map with all configurations
     */
    public OrderedConfig fillAllFiles(File directory, String... ignoreFiles) {
        OrderedConfig filledMap = new OrderedConfig();
        for (File directoryFile : directory.listFiles()) {
            String name = directoryFile.getName();
            if (ignoreFiles != null && ignoreFiles.length > 0) {
                boolean one = false;
                for (String ignore : ignoreFiles) {
                    if (name.startsWith(ignore)) {
                        one = true;
                        break;
                    }
                }
                if (one) {
                    continue;
                }
            }
            if (name.endsWith(".yml")) {
                OrderedConfig newFilledMap = fillOneFile(directoryFile);
                if (newFilledMap == null || newFilledMap.isEmpty()) {
                    continue;
                }
                filledMap.add(newFilledMap, true);
            } else if (directoryFile.isDirectory()) {
                filledMap.add(fillAllFiles(directoryFile), true);
            }
        }

        usePathToSerializersAndValidators(filledMap);

        if (filledMap.isEmpty()) {
            return new OrderedConfig();
        }
        return filledMap;
    }

    /**
     * Fills one file into map and returns its configuration.
     * This also takes in account given serializers.
     *
     * @param file the file to read
     * @return the map with file's configurations
     */
    public OrderedConfig fillOneFile(File file) {
        OrderedConfig filledMap = new OrderedConfig();
        String startsWithDeny = null;
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        for (String key : configuration.getKeys(true)) {
            if (startsWithDeny != null) {
                if (key.startsWith(startsWithDeny)) {
                    continue;
                }
                startsWithDeny = null;
            }
            String[] keySplit = key.split("\\.");
            if (keySplit.length > 0) {
                String lastKey = keySplit[keySplit.length - 1].toLowerCase();
                IValidator validator = this.validators.get(lastKey);
                if (validator != null) {
                    validatorDatas.add(new ValidatorData(validator, file, configuration, key));
                }

                Serializer<?> serializer = this.serializers.get(lastKey);
                if (serializer != null) {
                    String pathTo = serializer.useLater(configuration, key);
                    if (pathTo != null) {
                        startsWithDeny = key;
                        pathToSerializers.add(new PathToSerializer(serializer, key, pathTo));
                        continue;
                    }
                    Object valid;
                    try {
                        valid = serializer.serialize(file, configuration, key);
                    } catch (Exception e) {
                        DebugUtil.log(LogLevel.WARN, "Caught exception from serializer " + serializer.getKeyword() + "!", e);
                        continue;
                    }
                    if (valid != null) {
                        startsWithDeny = key;
                        filledMap.put(key, valid);
                        continue;
                    }
                }
            }
            Object object = configuration.get(key);
            if (object instanceof Boolean || object.toString().equalsIgnoreCase("true") || object.toString().equalsIgnoreCase("false")) {
                filledMap.put(key, Boolean.valueOf(object.toString()));
            } else if (object instanceof Number) {
                filledMap.put(key, ((Number) object).doubleValue());
            } else if (object instanceof List<?>) {
                filledMap.put(key, convertListObject(object));
            } else if (object instanceof String) {
                filledMap.put(key, colorizeString(object.toString()));
            }
        }
        if (filledMap.isEmpty()) {
            return null;
        }
        return filledMap;
    }

    /**
     * Uses all path to serializers and validators.
     * This should be used AFTER normal serialization.
     *
     * @param filledMap the filled mappings
     * @return the map with used path to serializers and validators
     */
    public OrderedConfig usePathToSerializersAndValidators(OrderedConfig filledMap) {
        if (!pathToSerializers.isEmpty()) {
            for (PathToSerializer pathToSerializer : pathToSerializers) {
                pathToSerializer.getSerializer().tryPathTo(filledMap, pathToSerializer.getPathWhereToStore(), pathToSerializer.getPathTo());
            }
        }
        if (!validatorDatas.isEmpty()) {
            for (ValidatorData validatorData : validatorDatas) {
                validatorData.getValidator().validate(filledMap, validatorData.getFile(), validatorData.getConfigurationSection(), validatorData.getPath());
            }
        }
        return filledMap;
    }

    private Set<String> convertListObject(Object object) {
        Set<String> list = new LinkedHashSet<>();
        for (Object obj : (List<?>) object) {
            list.add(colorizeString(obj.toString()));
        }
        return list;
    }

    private String colorizeString(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    /**
     * Class to hold path to serializer data for later on use
     */
    public static class PathToSerializer {

        private final Serializer<?> serializer;
        private final String pathWhereToStore;
        private final String pathTo;

        public PathToSerializer(Serializer<?> serializer, String pathWhereToStore, String pathTo) {
            this.serializer = serializer;
            this.pathWhereToStore = pathWhereToStore;
            this.pathTo = pathTo;
        }

        public Serializer<?> getSerializer() {
            return serializer;
        }

        public String getPathWhereToStore() {
            return pathWhereToStore;
        }

        public String getPathTo() {
            return pathTo;
        }
    }

    /**
     * Class to help validators
     */
    public static class ValidatorData {

        private final IValidator validator;
        private final File file;
        private final ConfigurationSection configurationSection;
        private final String path;

        public ValidatorData(IValidator validator, File file, ConfigurationSection configurationSection, String path) {
            this.validator = validator;
            this.file = file;
            this.configurationSection = configurationSection;
            this.path = path;
        }

        public IValidator getValidator() {
            return validator;
        }

        public File getFile() {
            return file;
        }

        public ConfigurationSection getConfigurationSection() {
            return configurationSection;
        }

        public String getPath() {
            return path;
        }
    }
}