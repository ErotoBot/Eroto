package xyz.eroto.bot.cogs

import xyz.eroto.bot.annotations.*
import xyz.eroto.bot.entities.Cog
import xyz.eroto.bot.entities.Context
import xyz.eroto.bot.entities.ICommand
import java.time.temporal.ChronoUnit

class Generic : Cog() {
    @Command
    @Description("Pong!")
    @CommandExample("eroto ping")
    class Ping : ICommand {
        override fun run(ctx: Context) {
            ctx.send("Pong!") {
                editMessage("Pong! `${ctx.event.message.creationTime.until(creationTime, ChronoUnit.MILLIS)}ms`").queue()
            }
        }
    }

    @Subcommand("ping")
    @CommandExample("eroto ping pong")
    class Pong : ICommand {
        override fun run(ctx: Context) {
            ctx.send("Ping!") {
                editMessage("Ping! `${ctx.event.message.creationTime.until(creationTime, ChronoUnit.MILLIS)}ms`").queue()
            }
        }
    }

    @Subcommand("pong")
    @CommandExample("eroto ping pong pang")
    class Pang : ICommand {
        override fun run(ctx: Context) {
            ctx.send("Peng!") {
                editMessage("Peng! `${ctx.event.message.creationTime.until(creationTime, ChronoUnit.MILLIS)}ms`").queue()
            }
        }
    }

    @Command
    @Description("Makes a choice for you")
    @Arguments(
            Argument("choices", "choice | choice | choice")
    )
    class Choose : ICommand {
        override fun run(ctx: Context) {
            val choices = (ctx.args["choices"] as String).split(Regex("\\s?\\|\\s?"))
            val choice = choices[Math.floor(Math.random() * choices.size).toInt()]

            ctx.send(choice)
        }
    }
}