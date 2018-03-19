package xyz.eroto.bot.entities

data class Argument<out T>(
        val name: String,
        val displayName: String? = null,
        val defaultValue: T? = null,
        val optional: Boolean = false
)