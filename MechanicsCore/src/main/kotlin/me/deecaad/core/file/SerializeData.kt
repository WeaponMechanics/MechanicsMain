package me.deecaad.core.file

import com.cjcrafter.foliascheduler.util.MinecraftVersions
import com.cjcrafter.foliascheduler.util.ReflectionUtil
import com.cryptomorin.xseries.XEntityType
import com.cryptomorin.xseries.XMaterial
import com.cryptomorin.xseries.particles.XParticle
import me.deecaad.core.file.SerializerException.Companion.builder
import me.deecaad.core.file.simple.EnumValueSerializer
import me.deecaad.core.file.simple.RegistryValueSerializer
import me.deecaad.core.utils.SerializerUtil.foundAt
import me.deecaad.core.utils.StringUtil.colorAdventure
import me.deecaad.core.utils.StringUtil.split
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Registry
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import java.io.File
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor

/**
 * [SerializeData] wraps a [ConfigurationSection] and a key along with useful
 * "validation methods". These methods will throw a [SerializerException] if the server admin
 * input an incorrect value. This allows us, the developers, to quickly and easily check if the
 * config is valid (Without long if/else if/else chains, or otherwise). Uses a builder pattern for
 * nice one-liners.
 *
 * For example, to get a positive integer from config, we can use
 * `SerializeData#of("your.key").assertExists().assertPositive().getInt()`.
 */
class SerializeData {
    val serializer: String
    val file: File
    val key: String?
    val config: ConfigLike

    /**
     * The fully serialized configuration to be used in case a nested-serializer uses the path-to
     * feature. This should not be read directly, instead let
     * [SerializeData.ConfigAccessor.serialize] check it automatically.
     */
    var pathToConfig: Configuration? = null

    /**
     * If this is true, developers are using [.step]. This is an advanced path-to
     * feature which allows developers to get values from config NOT STORED in the serialized object,
     * but still under the configuration section of the serializer. When this is true, we pull values
     * from 'pathToConfig' instead of 'config'
     */
    private var usingStep = false

    constructor(serializer: String, file: File, key: String?, config: ConfigLike) {
        this.serializer = serializer
        this.file = file
        this.key = key
        this.config = config
    }

    constructor(serializer: String, other: SerializeData, relative: String) {
        this.serializer = serializer
        this.file = other.file
        this.key = other.getPath(relative)
        this.config = other.config

        copyMutables(other)
    }

    constructor(serializer: Serializer<*>, file: File, key: String?, config: ConfigLike) {
        this.serializer = serializer.name
        this.file = file
        this.key = key
        this.config = config
    }

    constructor(serializer: Serializer<*>, other: SerializeData, relative: String) {
        this.serializer = serializer.name
        this.file = other.file
        this.key = other.getPath(relative)
        this.config = other.config

        copyMutables(other)
    }

    private fun copyMutables(from: SerializeData): SerializeData {
        this.usingStep = from.usingStep
        return this
    }

    /**
     * Returns the path to the key.
     *
     * @param relative The non-null relative path.
     * @return The total path + relative path.
     */
    private fun getPath(relative: String?): String? {
        return if (key == null || key.isEmpty()) relative else ("$key.$relative")
    }

    /**
     * Helper method to "move" into a new configuration section. The given relative key should
     * *always* point towards a [ConfigurationSection]
     *
     * @param relative The non-null, non-empty key relative to this.key.
     * @return The non-null serialize data.
     * @throws IllegalArgumentException If no configuration section exists at the location.
     */
    fun move(relative: String): SerializeData {
        return SerializeData(serializer, this, relative).copyMutables(this)
    }

    /**
     * The opposite of [move] method. This method will "step back" to the previous
     * configuration section. For example, if the current key is `a.b.c`, then this method will return
     * `a.b`.
     */
    fun back(): SerializeData {
        val split = key!!.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
        val key = StringBuilder()

        for (i in 0..<split.size - 1) key.append(split[i]).append('.')

        if (key.isNotEmpty()) key.setLength(key.length - 1)

        return SerializeData(serializer, file, key.toString(), config).copyMutables(this)
    }

    /**
     * Helper method to "step" into a new configuration section. Uses the
     * [Serializer.getKeyword] to step into the section. Supports using the
     * [Serializer.canUsePathTo] to step into other files instead of just nested configuration
     * sections.
     *
     * @param serializer The non-null serializer that supports path-to.
     * @return The non-null serialize data.
     * @throws SerializerException If no path-to config is defined.
     */
    @Throws(SerializerException::class)
    fun step(serializer: Serializer<*>): SerializeData {
        require(!(serializer.keyword == null || !serializer.canUsePathTo())) { "$serializer does not support path-to" }

        // Check that the user is trying to use path-to.
        val relative = serializer.keyword
        if (config is BukkitConfig && config.isString(getPath(relative))) {
            // This exception should be caught by FileReader so this serializer
            // is saved for late serialization (for path-to support).

            if (pathToConfig == null) throw PathToSerializerException(
                this, of().location, mutableListOf(
                    "You are using path-to, but you haven't defined the path-to configuration yet.",
                    "This is a bug in the plugin, please report this to the developer.",
                    "Serializer: " + serializer.name
                )
            )

            val path = config.getString(getPath(relative))
            val temp = SerializeData(serializer, file, path, config) // just pass 'config' for safety's sake
            temp.copyMutables(this)
            temp.usingStep = true
            return temp
        }

        // Just move in when not using path-to.
        return move(relative!!)
    }

    /**
     * Creates a [ConfigAccessor] which accesses the data (stored in config) at
     * `this.key + "." + relative`. The returned accessor can be used to validate arguments.
     *
     * @param relative The non-null, non-empty key relative to this.key.
     * @return The non-null config accessor.
     */
    @JvmOverloads
    fun of(relative: String? = null): ConfigAccessor {
        if (relative == null) {
            val split = key!!.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
            return back().of(split.last())
        }

        return ConfigAccessor(relative)
    }

    /**
     * Creates a [ConfigListAccessor] which accesses the data (stored in config) at
     * `this.key + ".' + relative`. The returned accessor can be used to validate arguments.
     *
     * @param relative The non-null, non-empty key relative to this.key.
     * @return The non-null config list accessor.
     */
    @JvmOverloads
    fun ofList(relative: String? = null): ConfigListAccessor {
        if (relative == null) {
            val split = key!!.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
            return back().ofList(split.last())
        }
        return ConfigListAccessor(relative)
    }

    /**
     * Returns `true` if the given relative config key exists. Otherwise, this method will
     * return false. Usually, you should use [ConfigAccessor.assertExists].
     *
     * @param relative The non-null relative key.
     * @return `true` if the key exists.
     */
    fun has(relative: String?): Boolean {
        return if (usingStep) pathToConfig!!.contains(getPath(relative)!!) else config.contains(getPath(relative))
    }

    /**
     * When there is no method in [ConfigAccessor] to match a specific configuration error, you
     * may check for it manually and use this method to throw a "general" exception.
     *
     * Make sure to keep messages clear and concise. There is no limit to how many messages you may give
     * to the player, but make sure that each message is *important* and contains *useful*
     * information.
     *
     * @param relative The nullable relative key.
     * @param messages The non-empty list of messages to include.
     * @return The non-null constructed exception.
     */
    fun exception(relative: String?, vararg messages: String): SerializerException {
        require(messages.isNotEmpty()) { "Hey you! Yeah you! Don't be lazy, add messages!" }

        var key = this.key
        if (!relative.isNullOrEmpty()) key = getPath(relative)

        return SerializerException(foundAt(file, key!!), mutableListOf(*messages))
    }

    /**
     * When there is no method in [ConfigListAccessor] to match a specific configuration error,
     * you may check for it manually and use this method to throw a "general" exception.
     *
     * @param relative The nullable relative key.
     * @param index The index (NOT index + 1) of the element that had the error.
     * @param messages The non-empty list of messages to include
     * @return The non-null constructed exception.
     */
    fun listException(relative: String?, index: Int, vararg messages: String): SerializerException {
        require(messages.isNotEmpty()) { "Hey you! Yeah you! Don't be lazy, add messages!" }

        var key = this.key
        if (!relative.isNullOrEmpty()) key = getPath(relative)

        return SerializerException(foundAt(file, key!!, index + 1), mutableListOf(*messages))
    }

    /**
     * Wraps a configuration KEY (which points to a list of values) to some helper functions to
     * facilitate data serialization. The
     */
    inner class ConfigListAccessor(private val relative: String?) {
        // Stores the class arguments, which is used to check the format
        private val arguments: MutableList<SimpleSerializer<*>> = ArrayList()
        private var requiredArgs: Int = 0

        fun <T : Any> addArgument(serializer: SimpleSerializer<T>): ConfigListAccessor {
            arguments.add(serializer)
            return this
        }

        fun requireAllPreviousArgs(): ConfigListAccessor {
            requiredArgs = arguments.size
            return this
        }

        /**
         * Asserts that this key exists in the configuration. This method ensures that the user explicitly
         * defined a value for the key.
         *
         * @return A non-null reference to this accessor (builder pattern).
         * @throws SerializerException If the key is not explicitly defined.
         */
        @Throws(SerializerException::class)
        fun assertExists(): ConfigListAccessor {
            if (!has(relative)) {
                throw builder()
                    .locationRaw(location)
                    .buildMissingRequiredKey(relative!!)
            }

            return this
        }

        @Throws(SerializerException::class)
        fun assertList(): List<List<Optional<Any>>> {
            check(arguments.isNotEmpty()) { "Need to set arguments before assertions" }

            // A formatted string like: <material*> <integer*> <true/false>
            // This helps the user understand what they need to put in
            val expectedInputFormat = StringBuilder("<")
            for (i in arguments.indices) {
                val arg = arguments[i]
                expectedInputFormat.append(arg.typeName)
                if (i < requiredArgs) expectedInputFormat.append('*')

                if (i != arguments.size - 1) expectedInputFormat.append("> <")
            }
            expectedInputFormat.append('>')

            // The first step is to assert that the value stored at this key
            // is a list (of any generic-type).
            val value = if (usingStep) pathToConfig!!.getObject(getPath(relative)!!) else config[getPath(relative)]
            if (value == null) return listOf()

            if (value !is List<*>) {
                throw builder()
                    .locationRaw(location)
                    .buildInvalidType("list of $expectedInputFormat", value)
            }

            // Use assertExists for required keys
            if (value.isEmpty()) return listOf()

            // The resulting list of parsed values
            val listOfParsedData: MutableList<List<Optional<Any>>> = ArrayList()

            for (i in value.indices) {
                val string = Objects.toString(value[i])
                val parsedData: MutableList<Optional<Any>> = ArrayList()

                // Empty string in config is probably a mistake (Perhaps they
                // forgot to save?). Instead of ignoring this, we should tell
                // the user (playing it safe).
                if (string == null || string.trim { it <= ' ' }.isEmpty()) {
                    throw listException(
                        relative, i, "$relative does not allow empty elements in the list.",
                        "Valid Format: $expectedInputFormat"
                    )
                }

                // Each element in the list should be a string of values
                // separated by a standard delimiter (Either '~' or '-' or ' ')
                val split = split(string)

                // Missing required data
                if (split.size < requiredArgs) {
                    throw listException(
                        relative, i, "$relative requires the first $requiredArgs arguments to be defined.",
                        "For value: $string",
                        "You are missing " + (requiredArgs - split.size) + " arguments",
                        "Valid Format: $expectedInputFormat"
                    )
                }

                for (j in split.indices) {
                    // Extra data check. This happens when the user adds more
                    // data than what the list can take. For example, if this
                    // list uses the format 'string-int' and the user inputs
                    // 'string-int-double', then this will be triggered.

                    if (arguments.size <= j) {
                        throw listException(
                            relative,
                            i,
                            "Invalid list format, " + relative + " can only use " + arguments.size + " arguments.",
                            "Found Value: $string",
                            "Valid Format: $expectedInputFormat"
                        )
                    }

                    val component = split[j]
                    val argument = arguments[j]
                    val parsedValue = argument.deserialize(component, getLocation(i))
                    parsedData.add(Optional.of(parsedValue))
                }

                // Fill up the rest of the arguments with empty values
                for (j in split.size until arguments.size) {
                    parsedData.add(Optional.empty<Any>())
                }

                listOfParsedData.add(parsedData)
            }

            return Collections.unmodifiableList(listOfParsedData)
        }

        val location: String
            get() {
                val stepAddon = if (usingStep) " (File location will be inaccurate since you are using path-to)" else ""
                return if (relative == null || relative.isEmpty()) {
                    config.getLocation(file, key) + stepAddon
                } else {
                    config.getLocation(file, getPath(relative)) + stepAddon
                }
            }

        fun getLocation(index: Int): String {
            val stepAddon = if (usingStep) " (File location will be inaccurate since you are using path-to)" else ""
            return if (relative == null || relative.isEmpty()) {
                foundAt(file, key!!, index + 1) + stepAddon
            } else {
                foundAt(file, getPath(relative)!!, index + 1) + stepAddon
            }
        }
    }

    /**
     * Wraps a configuration KEY to some helper functions to facilitate data serialization. The (public)
     * methods of this class will throw a [SerializerException] if the configuration is invalid.
     *
     * The methods of this class follow the Builder pattern.
     */
    inner class ConfigAccessor(private val relative: String) {
        private var exists = false

        /**
         * Asserts that this key exists in the configuration. This method ensures that the user explicitly
         * defined a value for the key.
         *
         * @return A non-null reference to this accessor (builder pattern).
         * @throws SerializerException If the key is not explicitly defined.
         */
        @Throws(SerializerException::class)
        fun assertExists(): ConfigAccessor {
            if (!has(relative)) {
                throw builder()
                    .locationRaw(location)
                    .buildMissingRequiredKey(relative)
            }
            this.exists = true

            return this
        }

        /**
         * Returns `true` when the object stored in this location matches the given
         * `type`.
         *
         * @param type Which type to check for
         * @return true, if the value matched the type.
         */
        fun `is`(type: Class<*>): Boolean {
            require(!type.isPrimitive) { "Silly developer, $type is a primitive type! Check wrapper classes instead." }
            val value = if (usingStep) pathToConfig!!.getObject(getPath(relative)!!) else config[getPath(relative)]

            return value != null && type.isAssignableFrom(value.javaClass)
        }

        /**
         * Asserts that the value at this key is an instance of the given class. Ensures that the datatype
         * matches what the developer expected the user to give.
         *
         * @param type The non-null data type to match.
         * @return A non-null reference to this accessor (builder pattern).
         * @throws SerializerException If the type does not match.
         */
        @Throws(SerializerException::class)
        fun assertType(type: Class<*>): ConfigAccessor {
            val value = if (usingStep) pathToConfig!!.getObject(getPath(relative)!!) else config[getPath(relative)]

            // Use assertExists for required keys
            if (value != null) {
                val actual: Class<*> = value.javaClass
                if (!type.isAssignableFrom(actual)) {
                    throw builder()
                        .locationRaw(location)
                        .buildInvalidType(type.simpleName, value)
                }
            }

            return this
        }

        /**
         * Asserts that the value at this key is a number of any type. The check is done by checking the
         * value can be type-casted to a double. Note that if you want a more specific number type (for
         * example, an integer), you should use [assertType].
         *
         * @return A non-null reference to this accessor (builder pattern).
         * @throws SerializerException If the type is not a number.
         */
        @Throws(SerializerException::class)
        fun getNumber(): Optional<Number> {
            var value = if (usingStep) pathToConfig!!.getObject(getPath(relative)!!) else config[getPath(relative)]

            // Use assertExists for required keys
            if (value == null) {
                return Optional.empty()
            }

            // If the value is a string, attempt to parse it as a number
            if (value is String) {
                try {
                    value = value.toDouble()
                } catch (ex: NumberFormatException) {
                    throw builder()
                        .locationRaw(location)
                        .buildInvalidType("number", value)
                }
            }

            try {
                return Optional.of(value as Number)
            } catch (ex: ClassCastException) {
                throw builder()
                    .locationRaw(location)
                    .buildInvalidType("number", value)
            }
        }

        /**
         * Returns the integer value of the config, or throws an exception if the value is not a number.
         * Note that this method will also throw an exception if the input is explicitly a double. For
         * example, `1.0` is a valid integer which will be parsed as `1`, but
         * `1.1` will throw an exception.
         *
         * @return The integer from config, or [OptionalInt.empty].
         * @throws SerializerException If the config value is not an integer.
         */
        @Throws(SerializerException::class)
        fun getInt(): OptionalInt {
            val num = getNumber()
            if (num.isEmpty)
                return OptionalInt.empty()

            val numValue = num.get().toDouble()
            if (floor(numValue).compareTo(ceil(numValue)) != 0) {
                throw builder()
                    .locationRaw(location)
                    .addMessage("Expected an integer WITHOUT any decimal (floating point) value")
                    .buildInvalidType("integer", num)
            }

            return OptionalInt.of(numValue.toInt())
        }

        /**
         * Returns the double value of the config, or throws an exception if the value is not a number.
         *
         * @return The double from config.
         * @throws SerializerException If the config value is not a double.
         */
        @Throws(SerializerException::class)
        fun getDouble(): OptionalDouble {
            val num = getNumber()
            if (num.isEmpty)
                return OptionalDouble.empty()

            return OptionalDouble.of(num.get().toDouble())
        }

        /**
         * Returns the boolean value of the config, or throws an exception if the value is not a boolean.
         *
         * @return The boolean from config.
         * @throws SerializerException If the config value is not a boolean.
         */
        @Throws(SerializerException::class)
        fun getBool(): Optional<Boolean> {
            val value = if (usingStep) pathToConfig!!.getObject(getPath(relative)!!) else config[getPath(relative)]
            if (value == null) {
                return Optional.empty()
            }

            if (value is Boolean) {
                return Optional.of(value)
            }

            if (value is String) {
                if (value.toString().trim().equals("true", ignoreCase = true)) return Optional.of(true)
                if (value.toString().trim().equals("false", ignoreCase = true)) return Optional.of(false)
            }

            throw builder()
                .locationRaw(location)
                .buildInvalidType("boolean", value)
        }

        /**
         * Asserts that the value at this key is a number, AND that the number is within the inclusive
         * range. Note that if you want a more specific number type (for example, an integer), you should
         * use [getInt].
         *
         * @param min Inclusive minimum bound.
         * @param max Inclusive maximum bound.
         * @return A non-null reference to this accessor (builder pattern).
         * @throws SerializerException If the value is not in range.
         * @throws IllegalArgumentException If min larger than max.
         */
        @Throws(SerializerException::class)
        fun assertRange(min: Int? = null, max: Int? = null): ConfigAccessor {
            if (min != null && max != null) require(min <= max) { "min > max" }
            if (min == null && max == null) throw IllegalArgumentException("min and max cannot be null")

            // Use assertExists for required keys
            val value = getNumber()
            if (value.isPresent) {
                // Silently strips away float point data (without exception)

                val num = value.get().toInt()
                if (min != null && num < min || max != null && num > max) {
                    throw builder()
                        .locationRaw(location)
                        .buildInvalidRange(num, min, max)
                }
            }

            return this
        }

        /**
         * Asserts that the value at this key is a number, AND that the number is within the inclusive
         * range. Note that if you want a more specific number type (for example, an integer), you should
         * use [getInt].
         *
         * @param min Inclusive minimum bound.
         * @param max Inclusive maximum bound.
         * @return A non-null reference to this accessor (builder pattern).
         * @throws SerializerException If the value is not in range.
         * @throws IllegalArgumentException If min larger than max.
         */
        @Throws(SerializerException::class)
        fun assertRange(min: Double? = null, max: Double? = null): ConfigAccessor {
            if (min != null && max != null) require(min <= max) { "min > max" }
            if (min == null && max == null) throw IllegalArgumentException("min and max cannot be null")

            // Use assertExists for required keys
            val value = getNumber()
            if (value.isPresent) {

                val num = value.get().toDouble()
                if (min != null && num < min || max != null && num > max) {
                    throw builder()
                        .locationRaw(location)
                        .buildInvalidRange(num, min, max)
                }
            }

            return this
        }

        val location: String
            get() {
                val stepAddon = if (usingStep) " (File location will be inaccurate since you are using path-to)" else ""
                return config.getLocation(file, getPath(relative)) + stepAddon
            }

        /**
         * Gets the data stored at this relative key. Note that this method (basically) requires a previous
         * call to [assertExists], especially for primitive types. When the key is optional, use
         * [get] to define a default value.
         *
         * @param <T> The expected data-type of the data.
         * @return The data stored at this relative key.
         */
        fun <T : Any> get(clazz: Class<T>): Optional<T> {
            assertType(clazz)
            val value = (if (usingStep) pathToConfig!!.getObject(getPath(relative)!!) else config[getPath(relative)])

            if (value == null) {
                return Optional.empty()
            }

            return Optional.of(clazz.cast(value))
        }

        /**
         * Serializes an enum value from config. If the key is not defined, then `defaultValue`
         * is returned. If the user defines a string that doesn't match any enum, an
         * exception is thrown.
         *
         * @param clazz The non-null enum class.
         * @param <T> The enum type.
         * @return The serialized enum type, or defaultValue.
         * @throws SerializerException If there is a misconfiguration in config.
        </T> */
        @Throws(SerializerException::class)
        fun <T : Enum<T>> getEnum(clazz: Class<T>): Optional<T> {
            val input = if (usingStep) pathToConfig!!.getObject(getPath(relative)!!, String::class.java) else config.getString(getPath(relative))

            // Use assertExists for required keys
            if (input.isNullOrBlank()) {
                return Optional.empty()
            }

            val firstEnumFound = EnumValueSerializer(clazz, false).deserialize(input, location).first()
            return Optional.of(firstEnumFound)
        }

        /**
         * Uses [XMaterial] to parse the material from the string.
         *
         * @return The material from config, or defaultValue.
         * @throws SerializerException If the user defined an invalid material.
         */
        @Throws(SerializerException::class)
        fun getMaterial(): Optional<XMaterial> {
            var input = if (usingStep) pathToConfig!!.getString(getPath(relative)!!) else config.getString(getPath(relative))

            // Use assertExists for required keys
            if (input.isNullOrBlank()) {
                return Optional.empty()
            }

            // Wildcards are not allowed for singleton enums, they are only
            // allowed for lists.
            input = input.trim()
            val xmat = XMaterial.matchXMaterial(input)
            if (xmat.isEmpty) {
                throw builder()
                    .locationRaw(location)
                    .buildInvalidEnumOption(input, Material::class.java)
            }

            val parsed = xmat.get()
            if (!parsed.isSupported) {
                throw exception(
                    relative,
                    "Your version, " + MinecraftVersions.getCurrent() + ", doesn't support '" + parsed.name + "'",
                    "Try using a different material or update your server to a newer version!"
                )
            }

            return Optional.of(parsed)
        }

        /**
         * Wraps [getMaterial] and returns the material as an [ItemStack], so you
         * don't have to depend on XSeries and relocate it.
         *
         * @return The material as an item, or defaultValue.
         * @throws SerializerException If the user defined an invalid material.
         */
        @Throws(SerializerException::class)
        fun getMaterialAsItem(): Optional<ItemStack> {
            val xmat = getMaterial()

            if (xmat.isEmpty) {
                return Optional.empty()
            }

            val parsed = xmat.get().parseItem()
                ?: throw exception(
                    relative,
                    "Your version, " + MinecraftVersions.getCurrent() + ", doesn't support '" + xmat.get().name + "'",
                    "Try using a different material or update your server to a newer version!"
                )

            return Optional.of(parsed)
        }

        /**
         * Uses [XEntityType] to parse the [EntityType].
         *
         * @return The entity type from config, or defaultValue.
         * @throws SerializerException If the user defined an invalid entity type.
         */
        @Throws(SerializerException::class)
        fun getEntityType(): Optional<EntityType> {
            var input = if (usingStep) pathToConfig!!.getString(getPath(relative)!!) else config.getString(getPath(relative))

            // Use assertExists for required keys
            if (input.isNullOrBlank()) {
                return Optional.empty()
            }

            // Wildcards are not allowed for singleton enums, they are only
            // allowed for lists.
            input = input.trim()
            val entityType = XEntityType.of(input)
            if (entityType.isEmpty) {
                throw builder()
                    .locationRaw(location)
                    .buildInvalidEnumOption(input, EntityType::class.java)
            }

            val parsed = entityType.get().get()
                ?: throw exception(
                    relative,
                    "Your version, " + MinecraftVersions.getCurrent() + ", doesn't support '" + entityType.get().name + "'",
                    "Try using a different material or update your server to a newer version!"
                )

            return Optional.of(parsed)
        }

        /**
         * Uses [XParticle] to parse the [Particle].
         *
         * @return The particle from config, or defaultValue.
         * @throws SerializerException If the user defined an invalid particle.
         */
        @Throws(SerializerException::class)
        fun getParticle(): Optional<Particle> {
            var input = if (usingStep) pathToConfig!!.getString(getPath(relative)!!) else config.getString(getPath(relative))

            // Use assertExists for required keys
            if (input.isNullOrBlank()) {
                return Optional.empty()
            }

            // Wildcards are not allowed for singleton enums, they are only
            // allowed for lists.
            input = input.trim()
            val particle = XParticle.of(input)
            if (particle.isEmpty) {
                throw builder()
                    .locationRaw(location)
                    .buildInvalidEnumOption(input, Particle::class.java)
            }

            val parsed = particle.get().get()
                ?: throw exception(
                    relative,
                    "Your version, " + MinecraftVersions.getCurrent() + ", doesn't support '" + particle.get().name + "'",
                    "Try using a different material or update your server to a newer version!"
                )

            return Optional.of(parsed)
        }

        /**
         * Parses any item from a bukkit registry. Check the [Registry] class
         * for more information.
         */
        @Throws(SerializerException::class)
        fun <T : Keyed> getBukkitRegistry(registry: Registry<T>): Optional<T> {
            val input = if (usingStep) pathToConfig!!.getString(getPath(relative)!!) else config.getString(getPath(relative))

            // Use assertExists for required keys
            if (input.isNullOrBlank()) {
                return Optional.empty()
            }

            val firstItemFound = RegistryValueSerializer(registry, false).deserialize(
                input.trim().lowercase(),
                location
            ).first()

            return Optional.of(firstItemFound)
        }

        /**
         * Returns the string value of the config, adjusted to fit the adventure format. Adventure text is
         * formatting using html-like tags instead of the legacy `&` symbol. If the
         * string in config contains the legacy color system, we will attempt to convert it.
         *
         *
         * The returned string should be parsed using
         * [net.kyori.adventure.text.minimessage.MiniMessage]. You may use MechanicsCore's instance
         * [me.deecaad.core.MechanicsCore.message].
         *
         * @return The converted string from config.
         */
        fun getAdventure(): Optional<String> {
            val value = if (usingStep) pathToConfig!!.getObject(getPath(relative)!!, String::class.java) else config.getString(getPath(relative))

            // Use assertExists for required keys
            if (value.isNullOrBlank()) {
                return Optional.empty()
            }

            return Optional.of(colorAdventure(value))
        }

        /**
         * Returns one type from a registry. The exact type is unknown, and is determined by the
         * [InlineSerializer.UNIQUE_IDENTIFIER] present in configuration. If no value has been defined
         * in config, then the default value is returned.
         *
         * @param registry The non-null registry of possible types to use.
         * @param <T> The superclass type.
         * @return A serialized instance.
         * @throws SerializerException If there are any errors in config.
         */
        @Throws(SerializerException::class)
        fun <T : InlineSerializer<T>> getRegistry(registry: me.deecaad.core.mechanics.Registry<T>): Optional<T> {
            if (config !is MapConfigLike) throw UnsupportedOperationException("Cannot use registries with $config")
            if (!has(relative)) {
                return Optional.empty()
            }

            val map = assertExists().get(MutableMap::class.java)
            val temp: ConfigLike = MapConfigLike(map.get() as MutableMap<String, MapConfigLike.Holder>)
                .setDebugInfo(config.file, config.path, config.fullLine)
            val nested = SerializeData(serializer, file, null, temp)

            val key = nested.of(InlineSerializer.UNIQUE_IDENTIFIER).assertExists().get(String::class.java).get()
            val base = registry[key]
                ?: throw builder()
                    .locationRaw(location)
                    .buildInvalidOption(key, registry.options)

            return Optional.of(base.serialize(nested))
        }

        /**
         * This method is similar to [getRegistry], but instead of allowing every type from
         * a registry, 1 specific type is allowed.
         *
         * @param impliedType The serializer.
         * @param <T> The type to create.
         * @return The serialized instance.
         * @throws SerializerException If there are any errors in config.
         */
        @Throws(SerializerException::class)
        fun <T : Any> getImplied(impliedType: Serializer<T>): Optional<T> {
            if (config !is MapConfigLike) throw UnsupportedOperationException("Cannot use registries with $config")
            if (!has(relative)) {
                return Optional.empty()
            }

            val map = config[getPath(relative)] as Map<String, *>
            val identifier = map[InlineSerializer.UNIQUE_IDENTIFIER]

            // We have to make sure that the user used the "JSON Format" in the string.
            if (identifier != null && !me.deecaad.core.mechanics.Registry.matches(identifier.toString(), impliedType.keyword)) {
                throw exception(
                    relative,
                    "Expected a '${impliedType.keyword}' but got a '$identifier'"
                )
            }

            val temp: ConfigLike = MapConfigLike(map as Map<String, MapConfigLike.Holder?>).setDebugInfo(
                config.file,
                config.path,
                config.fullLine
            )

            val nested = SerializeData(impliedType, file, null, temp)
            return Optional.of(impliedType.serialize(nested))
        }

        @Throws(SerializerException::class)
        fun <T : InlineSerializer<T>> getRegistryList(registry: me.deecaad.core.mechanics.Registry<T>): List<T> {
            if (config !is MapConfigLike) throw UnsupportedOperationException("Cannot use registries with $config")
            if (!has(relative)) return listOf()

            val list = config.getList(getPath(relative)) as List<MapConfigLike.Holder?>
            val returnValue: MutableList<T> = ArrayList()

            for (i in list.indices) {
                val map = list[i]!!.value as? Map<*, *> ?: throw listException(
                    relative, i,
                    "Expected an inline serializer like 'sound(sound=ENTITY_GENERIC_EXPLOSION)', but instead got '${list[i]!!.value}'"
                )

                val id = (map[InlineSerializer.UNIQUE_IDENTIFIER] as? MapConfigLike.Holder)?.value?.toString()
                    ?: throw listException(
                        relative, i,
                        "Missing name for a(n) '$serializer'"
                    )

                val serializer: T = registry[id]
                    ?: throw builder()
                        .locationRaw(location)
                        .buildInvalidOption(id, registry.options)

                val temp: ConfigLike = MapConfigLike(map as Map<String, MapConfigLike.Holder>).setDebugInfo(
                    config.file,
                    config.path,
                    config.fullLine
                )

                val nested = SerializeData(serializer, file, null, temp)
                returnValue.add(serializer.serialize(nested))
            }

            return returnValue
        }

        @Throws(SerializerException::class)
        fun <T : InlineSerializer<T>> getImpliedList(impliedType: T): List<T> {
            if (config !is MapConfigLike) throw UnsupportedOperationException("Cannot use registries with $config")
            if (!has(relative)) return listOf()

            val list = config.getList(getPath(relative)) as List<MapConfigLike.Holder?>
            val returnValue: MutableList<T> = ArrayList()

            for (i in list.indices) {
                val map = list[i]!!.value as? Map<*, *> ?: throw listException(
                    relative, i,
                    "Expected an inline serializer like 'sound(sound=ENTITY_GENERIC_EXPLOSION)', but instead got '${list[i]!!.value}'"
                )

                val identifier = map[InlineSerializer.UNIQUE_IDENTIFIER]
                if (identifier != null && !me.deecaad.core.mechanics.Registry.matches(identifier.toString(), impliedType.keyword)) {
                    throw listException(
                        relative, i,
                        "Expected a '${impliedType.inlineKeyword}' but got a '$identifier'"
                    )
                }

                val temp: ConfigLike = MapConfigLike(map as Map<String?, MapConfigLike.Holder?>).setDebugInfo(
                    config.file,
                    config.path,
                    config.fullLine
                )

                val nested = SerializeData(impliedType, file, null, temp)
                returnValue.add(impliedType.serialize(nested))
            }

            return returnValue
        }

        /**
         * Uses the given class as a serializer and attempts to serialize an
         * object from this relative key.
         *
         * @param serializerClass The non-null serializer class.
         * @param <S> The serializer type.
         * @param <T> The serialized type.
         * @return The serialized object.
         * @throws SerializerException If there is a mistake in config found during serialization.
         */
        @Throws(SerializerException::class)
        fun <S : Serializer<T>, T : Any> serialize(serializerClass: Class<S>): Optional<T> {
            val serializer = ReflectionUtil.getConstructor(serializerClass).newInstance()
            return serialize(serializer)
        }

        /**
         * Uses the given serializer and attempts to serialize an object from
         * this relative key.
         *
         * @param serializer The non-null serializer.
         * @param <S> The serializer type.
         * @param <T> The serialized type.
         * @return The serialized object.
         */
        @Throws(SerializerException::class)
        fun <S : Serializer<T>, T : Any> serialize(serializer: S): Optional<T> {
            // Use assertExists for required keys
            if (!has(relative)) {
                return Optional.empty()
            }

            val data = SerializeData(serializer, this@SerializeData, relative)
            data.copyMutables(this@SerializeData)

            // Allow path-to compatibility when using nested serializers
            val isString = if (usingStep) pathToConfig!!.getObject(
                getPath(relative)!!,
                String::class.java
            ) == null else config.isString(getPath(relative))
            if (serializer.canUsePathTo() && isString) {
                if (usingStep) throw exception(
                    relative,
                    "Tried to use doubly nested path-to. This is is not a supported option."
                )

                val path = config.getString(getPath(relative))

                // In order for path-to to work, the serializer needs to have a
                // keyword so the FileReader automatically serializes it.
                if (serializer.keyword == null) throw PathToSerializerException(
                    data, location, mutableListOf(
                        "'${serializer.javaClass.simpleName}' does not have a keyword, so it cannot be used for path-to",
                        "This means you are trying to use an unsupported operation, and you cannot use serializers this way"
                    )
                )

                // If we don't have access to the serialized config, we cannot
                // attempt a path-to.
                if (pathToConfig == null) throw PathToSerializerException(
                    data, location, mutableListOf(
                        "Path-to is not supported in this context",
                        "To support path-to, we need access to the serialized config"
                    )
                )

                // Check to make sure the path points to a serialized object
                val obj = pathToConfig!!.getObject(path)
                    ?: throw exception(
                        relative, "Found an invalid path when using 'Path To' feature",
                        "Path '$path' could not be found. Check for errors above this message."
                    )

                // Technically not "perfect" since a serializer can return a
                // non-serializer object. ItemSerializer is covered with its
                // own item-registry system, and other cases are unlikely to
                // happen since the config is too small for them.
                if (!serializer.javaClass.isInstance(obj)) throw exception(
                    relative, "Found an invalid object when using 'Path To' feature",
                    "Path '$path' pointed to an improper object type.",
                    "Should have been '${serializer.javaClass.simpleName}', but instead got '${obj.javaClass.simpleName}'",
                    "For value: $obj"
                )

                // Generic fuckery
                return Optional.of(serializer.javaClass.cast(obj) as T)
            }

            return Optional.of(serializer.serialize(data))
        }
    }
}