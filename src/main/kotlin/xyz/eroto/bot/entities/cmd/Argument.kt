package xyz.eroto.bot.entities.cmd

import kotlin.reflect.KClass

data class Argument<T : Any>(
        val name: String,
        val displayName: String? = null,
        val defaultValue: T? = null,
        val optional: Boolean = false,
        val delimiter: String = ",",
        internal val clazz: KClass<T>
)

inline fun<reified T : Any> argument(
        name: String,
        displayName: String? = null,
        defaultValue: T? = null,
        optional: Boolean = false,
        delimiter: String = ","
) = Argument(name, displayName, defaultValue, optional, delimiter, T::class)