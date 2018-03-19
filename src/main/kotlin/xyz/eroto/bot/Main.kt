package xyz.eroto.bot

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import xyz.eroto.bot.entities.Config

fun main(args: Array<String>) {
    val mapper = ObjectMapper(YAMLFactory())

    mapper.registerModule(KotlinModule())

    val config = if (System.getenv("USE_ENV") != null) {
        Config(
                System.getenv("BOT_TOKEN") ?: return println("Token not found!"),
                System.getenv("BOT_PREFIXES").split("::"),
                System.getenv("BOT_SHARDS")?.toInt() ?: 1,
                System.getenv("BOT_FIRST_SHARD")?.toInt() ?: 0,
                System.getenv("BOT_LAST_SHARD")?.toInt() ?: 0
        )
    } else {
        mapper.readValue(File("config.yml"))
    }

    val bot = Eroto(config)

    bot.run()
}
