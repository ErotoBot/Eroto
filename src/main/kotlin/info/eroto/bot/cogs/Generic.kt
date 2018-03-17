package info.eroto.bot.cogs

import info.eroto.bot.annotations.Command
import info.eroto.bot.annotations.CommandExample
import info.eroto.bot.annotations.Description
import info.eroto.bot.annotations.Subcommand
import info.eroto.bot.entities.Cog
import info.eroto.bot.entities.Context
import info.eroto.bot.entities.ICommand
import java.time.temporal.ChronoUnit

class Generic : Cog() {
    @Command
    @Description("Pong!")
    @CommandExample("eroto ping")
    class Ping : ICommand {
        override fun run(ctx: Context) {
            ctx.event.channel.sendMessage("Pong!").queue { msg ->
                msg.editMessage("Pong! `${ctx.event.message.creationTime.until(msg.creationTime, ChronoUnit.MILLIS)}ms`").queue()
            }
        }
    }

    @Subcommand("ping")
    @CommandExample("eroto ping pong")
    class Pong : ICommand {
        override fun run(ctx: Context) {
            ctx.event.channel.sendMessage("Ping!").queue { msg ->
                msg.editMessage("Ping! `${ctx.event.message.creationTime.until(msg.creationTime, ChronoUnit.MILLIS)}ms`").queue()
            }
        }
    }

    @Subcommand("pong")
    @CommandExample("eroto ping pong pang")
    class Pang : ICommand {
        override fun run(ctx: Context) {
            ctx.event.channel.sendMessage("Peng!").queue { msg ->
                msg.editMessage("Peng! `${ctx.event.message.creationTime.until(msg.creationTime, ChronoUnit.MILLIS)}ms`").queue()
            }
        }
    }
}