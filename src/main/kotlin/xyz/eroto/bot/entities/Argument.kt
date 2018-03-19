package xyz.eroto.bot.entities

import kotlin.reflect.KClass

data class Argument<T : Any>(
        val name: String,
        val displayName: String? = null,
        val defaultValue: T? = null,
        val optional: Boolean = false,
        internal val clazz: KClass<T>
)

inline fun<reified T : Any> argument(
        name: String,
        displayName: String? = null,
        defaultValue: T? = null,
        optional: Boolean = false
) = Argument(name, displayName, defaultValue, optional, T::class)