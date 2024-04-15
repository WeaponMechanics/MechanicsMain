package me.deecaad.core.file

import kotlin.jvm.Throws

/**
 * Organizes all configuration files into some collection of key-value pairs,
 * where the key is always a string.
 */
abstract class Configuration : Iterable<Map.Entry<String, Any>> {
    // Mutators

    /**
     * Sets the value of the property with the specified [key] in this configuration.
     *
     * If the property is not present in this configuration, it is added.
     *
     * @param key the key of the property.
     * @param value the value of the property.
     */
    abstract fun set(
        key: String,
        value: Any?,
    )

    /**
     * Adds all properties from the specified [other] configuration to this configuration. If a key
     * from [other] is already present in this configuration, a [DuplicateKeyException] is thrown.
     *
     * @param other The other configuration to copy from.
     */
    @Throws(DuplicateKeyException::class)
    abstract fun copyFrom(other: Configuration)

    /**
     * Removes all key-value pairs from this configuration.
     */
    abstract fun clear()

    // Accessors

    /**
     * Returns an immutable set of all keys in this configuration.
     */
    abstract fun keys(): Set<String>

    /**
     * Returns an immutable set of all entries in this configuration.
     */
    abstract fun entries(): Set<Map.Entry<String, Any>>

    /**
     * Returns an immutable collection of all values in this configuration.
     */
    abstract fun values(): Collection<Any>

    /**
     * Returns true if this configuration contains the specified [key]. Otherwise, returns false.
     *
     * @param key the key to search for.
     * @return true if the key is present, false otherwise.
     */
    abstract operator fun contains(key: String): Boolean

    /**
     * Returns true if this configuration contains the specified [key], and that value is a boolean
     * value. Otherwise, returns false.
     */
    abstract fun hasBoolean(key: String): Boolean

    /**
     * Returns the value of the boolean property with the specified [key] in this configuration.
     *
     * If the property is not present in this configuration (when [hasBoolean] returns false),
     * this method returns the specified default ([def]).
     *
     * @param key the key of the property.
     * @param def the default value, if the property is not present. Defaults to false.
     * @return the value of the property, or the default value
     */
    @JvmOverloads
    fun getBoolean(
        key: String,
        def: Boolean = false,
    ): Boolean {
        return getBoolean0(key, def)
    }

    // internal method to get the boolean value
    protected abstract fun getBoolean0(
        key: String,
        def: Boolean,
    ): Boolean

    /**
     * Returns true if this configuration contains the specified [key], and that value is an integer
     * value. Otherwise, returns false.
     */
    abstract fun hasInt(key: String): Boolean

    /**
     * Returns the value of the integer property with the specified [key] in this configuration.
     *
     * If the property is not present in this configuration (when [hasInt] returns false),
     * this method returns the specified default ([def]).
     *
     * @param key the key of the property.
     * @param def the default value, if the property is not present. Defaults to 0.
     * @return the value of the property, or the default value
     */
    @JvmOverloads
    fun getInt(
        key: String,
        def: Int = 0,
    ): Int {
        return getInt0(key, def)
    }

    // internal method to get the integer value
    protected abstract fun getInt0(
        key: String,
        def: Int,
    ): Int

    /**
     * Returns true if this configuration contains the specified [key], and that value is a double
     * value. Otherwise, returns false.
     */
    abstract fun hasDouble(key: String): Boolean

    /**
     * Returns the value of the double property with the specified [key] in this configuration.
     *
     * If the property is not present in this configuration (when [hasDouble] returns false),
     * this method returns the specified default ([def]).
     *
     * @param key the key of the property.
     * @param def the default value, if the property is not present. Defaults to 0.0.
     * @return the value of the property, or the default value
     */
    @JvmOverloads
    fun getDouble(
        key: String,
        def: Double = 0.0,
    ): Double {
        return getDouble0(key, def)
    }

    // internal method to get the double value
    protected abstract fun getDouble0(
        key: String,
        def: Double,
    ): Double

    /**
     * Returns true if this configuration contains the specified [key], and that value is an object
     * value. Otherwise, returns false.
     */
    abstract fun hasObject(key: String): Boolean

    /**
     * Returns the value of the object property with the specified [key] in this configuration.
     *
     * If the property is not present in this configuration (when [hasObject] returns false),
     * this method returns null.
     *
     * @param key the key of the property.
     * @return the value of the property, or null
     */
    fun getObject(key: String): Any? {
        return getObject0(key, null, Any::class.java)
    }

    /**
     * Returns the value of the object property with the specified [key] in this configuration.
     *
     * If the property is not present in this configuration (when [hasObject] returns false),
     * this method returns the specified default ([def]).
     *
     * @param key the key of the property.
     * @param def the default value, if the property is not present.
     * @return the value of the property, or the default value
     */
    fun getObject(
        key: String,
        def: Any,
    ): Any {
        return getObject0(key, def, Any::class.java)!!
    }

    /**
     * Returns the value of the object property with the specified [key] in this configuration.
     *
     * If the property is not present in this configuration (when [hasObject] returns false),
     * this method returns null.
     *
     * @param key the key of the property.
     * @param clazz the class of the object to be returned.
     * @return the value of the property, or null
     */
    fun <T : Any> getObject(
        key: String,
        clazz: Class<T>,
    ): T? {
        return getObject0(key, null, clazz)
    }

    /**
     * Returns the value of the object property with the specified [key] in this configuration.
     *
     * If the property is not present in this configuration (when [hasObject] returns false),
     * this method returns the specified default ([def]).
     *
     * @param key the key of the property.
     * @param def the default value, if the property is not present. Defaults to null.
     * @param clazz the class of the object to be returned.
     * @return the value of the property, or the default value
     */
    fun <T : Any> getObject(
        key: String,
        def: T,
        clazz: Class<T>,
    ): T {
        return getObject0(key, def, clazz)!!
    }

    /**
     * Returns the value of the object property with the specified [key] in this configuration.
     *
     * If the property is not present in this configuration (when [hasObject] returns false),
     * this method returns null.
     *
     * @param key the key of the property.
     * @return the value of the property, or null
     */
    inline fun <reified T : Any> get(key: String): T? {
        return getObject(key, T::class.java)
    }

    /**
     * Returns the value of the object property with the specified [key] in this configuration.
     *
     * If the property is not present in this configuration (when [hasObject] returns false),
     * this method returns the specified default ([def]).
     *
     * @param key the key of the property.
     * @param def the default value, if the property is not present. Defaults to null.
     * @return the value of the property, or the default value
     */
    inline fun <reified T : Any> get(
        key: String,
        def: T,
    ): T {
        return getObject(key, def, T::class.java)
    }

    // internal method to get the object value
    protected abstract fun <T : Any> getObject0(
        key: String,
        def: T?,
        clazz: Class<T>,
    ): T?

    /**
     * Returns the value of the string property with the specified [key] in this configuration.
     * If the property is not present in this configuration, this method returns null.
     *
     * @param key the key of the property.
     * @return the value of the property, or null
     */
    fun getString(key: String): String? {
        return getObject(key, String::class.java)
    }

    /**
     * Returns the value of the string property with the specified [key] in this configuration.
     * If the property is not present in this configuration, this method returns the specified default ([def]).
     *
     * @param key the key of the property.
     * @param def the default value, if the property is not present.
     * @return the value of the property, or the default value
     */
    fun getString(
        key: String,
        def: String,
    ): String {
        return getObject(key, def, String::class.java)
    }
}
