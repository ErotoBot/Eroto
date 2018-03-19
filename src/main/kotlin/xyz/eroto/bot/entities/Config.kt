package xyz.eroto.bot.entities

data class Config(
        val token: String,
        val prefixes: List<String>,
        val shards: Int = 1,
        val firstShard: Int = 0,
        val lastShard: Int = 0
)