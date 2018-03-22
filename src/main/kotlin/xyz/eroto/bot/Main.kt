package xyz.eroto.bot

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import xyz.eroto.bot.entities.APIConfig
import java.io.File
import xyz.eroto.bot.entities.Config
import xyz.eroto.bot.entities.PostgresConfig

fun main(args: Array<String>) {
    val mapper = ObjectMapper(YAMLFactory())

    mapper.registerModule(KotlinModule())

    val config = if (System.getenv("USE_ENV") != null) {
        Config(
                System.getenv("BOT_TOKEN") ?: return println("Token not found!"),
                System.getenv("BOT_PREFIXES").split("::"),
                System.getenv("BOT_SHARDS")?.toInt() ?: 1,
                System.getenv("BOT_FIRST_SHARD")?.toInt() ?: 0,
                System.getenv("BOT_LAST_SHARD")?.toInt() ?: 0,
                System.getenv("BOT_WEEBSHTOKEN") ?: "",
                PostgresConfig(
                        System.getenv("POSTGRES_HOST") ?: "localhost",
                        System.getenv("POSTGRES_USER") ?: System.getenv("USERNAME") ?: return println("No user configured!"),
                        System.getenv("POSTGRES_PASSWORD") ?: "",
                        System.getenv("POSTGRES_DATABASE") ?: "eroto"
                ),
                APIConfig(
                        System.getenv("WEEBSH_TOKEN") ?: return println("No weebsh token given!")
                )
        )
    } else {
        mapper.readValue(File("config.yml"))
    }

    val bot = Eroto(config)

    bot.run()
}
