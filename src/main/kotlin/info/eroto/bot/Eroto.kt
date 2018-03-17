package info.eroto.bot

import info.eroto.bot.entities.Config
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.bot.sharding.ShardManager

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

        val manager = CogManager()
    }
}