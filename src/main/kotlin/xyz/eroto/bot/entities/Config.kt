package xyz.eroto.bot.entities

data class Config(
        val token: String,
        val shards: Int,
        val firstShard: Int,
        val lastShard: Int,
        val prefixes: List<String>
)