package xyz.eroto.bot.entities.db

import org.json.JSONObject

data class StoredGuild(
        val id: Long,
        val prefixes: List<String>,
        val mutedRole: Long?,
        val perms: List<String>
)