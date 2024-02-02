package me.deecaad.core.file;

import java.util.List;

public interface IValidator {

    default boolean denyKeys() {
        return false;
    }

    /**
     * @return keyword of this validator used in configurations
     */
    String getKeyword();

    /**
     * This validator is only used if the path {weapon title}.{getAllowedPaths()} matches fully.
     *
     * @return The nullable allowed paths
     */
    default List<String> getAllowedPaths() {
        return null;
    }

    /**
     * After the {@link #getKeyword()} check, this check can be customized by the validator in order to
     * "fine tune" when a validator should be automatically used.
     *
     * @param data The config information.
     * @return true if the validator should be used.
     */
    default boolean shouldValidate(SerializeData data) {
        return true;
    }

    /**
     * This is used to validate configurations which can't be used as serializers. This validation
     * process should be done after the serialization.
     *
     * @param configuration the global configuration object.
     * @param data Config wrapper, see {@link SerializeData}.
     */
    void validate(Configuration configuration, SerializeData data) throws SerializerException;
}