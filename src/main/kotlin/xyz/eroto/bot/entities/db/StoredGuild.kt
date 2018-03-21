package xyz.eroto.bot.entities.db

data class StoredGuild(
        val id: Long,
        val prefixes: List<String>,
        val mutedRole: String
)