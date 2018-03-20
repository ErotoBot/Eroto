package xyz.eroto.bot

import me.aurieh.ares.exposed.async.asyncTransaction
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.bot.sharding.ShardManager
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import xyz.eroto.bot.entities.Config
import xyz.eroto.bot.entities.CoroutineDispatcher
import xyz.eroto.bot.entities.schema.GuildsTable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Eroto(private val config: Config) {
    init {
        Eroto.config = config

        Database.connect(
                "jdbc:postgresql://${config.postgres.host}/${config.postgres.database}",
                "org.postgresql.Driver",
                config.postgres.username,
                config.postgres.password
        )

        asyncTransaction(pool) {
            SchemaUtils.createMissingTablesAndColumns(GuildsTable)
        }.execute()
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

        val pool: ExecutorService by lazy {
            Executors.newCachedThreadPool {
                Thread(it, "Eroto-Main-Pool-Thread").apply {
                    isDaemon = true
                }
            }
        }
        val coroutineDispatcher by lazy {
            CoroutineDispatcher(pool)
        }
    }
}