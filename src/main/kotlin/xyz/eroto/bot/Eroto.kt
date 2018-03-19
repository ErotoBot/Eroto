package xyz.eroto.bot

import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.bot.sharding.ShardManager
import xyz.eroto.bot.entities.Config

class Eroto(private val config: Config) {
    init {
        Eroto.config = config
    }

    fun run() {
        shardManager = DefaultShardManagerBuilder().apply {
            setToken(config.token)
            addEventListeners(EventListener())
            setAutoReconnect(true)
            setShardsTotal(config.shards)
            setShards(config.firstShard, config.lastShard)
        }.build()
    }

    companion object {
        lateinit var shardManager: ShardManager
        lateinit var config: Config

        val cmdManager = CommandManager()
    }
}