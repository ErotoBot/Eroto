package info.eroto.bot.cogs

import info.eroto.bot.annotations.Command
import info.eroto.bot.annotations.Subcommand
import info.eroto.bot.entities.Cog
import info.eroto.bot.entities.Context
import java.time.temporal.ChronoUnit

class Generic : Cog() {
    @Command
    fun ping(ctx: Context) {
        ctx.event.channel.sendMessage("Pong!").queue { msg ->
            msg.editMessage("Pong! `${ctx.event.message.creationTime.until(msg.creationTime, ChronoUnit.MILLIS)}ms`").queue()
        }
    }

    @Subcommand("ping")
    fun pong(ctx: Context) {
        ctx.event.channel.sendMessage("Ping!").queue { msg ->
            msg.editMessage("Ping! `${ctx.event.message.creationTime.until(msg.creationTime, ChronoUnit.MILLIS)}ms`").queue()
        }
    }
}