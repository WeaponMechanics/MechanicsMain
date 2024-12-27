package me.deecaad.core.file.simple

import me.deecaad.core.file.SerializerException
import me.deecaad.core.file.SimpleSerializer
import me.deecaad.core.utils.EnumUtil

class EnumValueSerializer<T : Enum<T>>(
    private val enumClass: Class<T>,
    private val isAllowWildcard: Boolean,
) : SimpleSerializer<List<T>> {
    override fun getTypeName(): String = enumClass.simpleName

    override fun deserialize(
        data: String,
        errorLocation: String,
    ): List<T> {
        var data = data.trim().lowercase()
        var isWildcard = false

        if (data.startsWith("$")) {
            data = data.substring(1)
            isWildcard = true

            if (!isAllowWildcard) {
                throw SerializerException.builder()
                    .locationRaw(errorLocation)
                    .addMessage("Wildcards are now allowed here... Remove the wildcard symbol '$'")
                    .addMessage("For value: $data")
                    .build()
            }
        }

        if (isWildcard) {
            val values = EnumUtil.parseEnums(enumClass, data)
            if (values.isEmpty()) {
                throw SerializerException.builder()
                    .locationRaw(errorLocation)
                    .addMessage("Wildcard '$$data' did not match any values.")
                    .addMessage("No values found that contain '$data' in their key.")
                    .buildInvalidEnumOption(data, enumClass)
            }
            return values
        } else {
            val value =
                EnumUtil.parseEnums(enumClass, data)
                    ?: throw SerializerException.builder()
                        .locationRaw(errorLocation)
                        .buildInvalidEnumOption(data, enumClass)

            return value
        }
    }

    override fun examples(): MutableList<String> {
        return EnumUtil.getOptions(enumClass).toMutableList()
    }
}
