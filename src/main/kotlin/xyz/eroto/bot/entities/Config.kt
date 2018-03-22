package xyz.eroto.bot.entities

data class PostgresConfig(
        val host: String,
        val username: String,
        val password: String,
        val database: String
)

data class APIConfig(
        val weebsh: String
)

data class Config(
        val token: String,
        val prefixes: List<String>,
        val shards: Int = 1,
        val firstShard: Int = 0,
        val lastShard: Int = 0,
        val postgres: PostgresConfig,
        val api: APIConfig
)