package xyz.eroto.bot.commands

import xyz.eroto.bot.entities.*

class Pang : Subcommand() {
    override val description = "Peng!"
    override val example = "ping pong pang"

    override fun run(ctx: Context) {
        ctx.send("Peng!")
    }
}

class Pong : Subcommand() {
    override val description = "Ping!"
    override val example = "ping pong"

    init {
        subcommands += Pang()
    }

    override fun run(ctx: Context) {
        ctx.send("Ping!")
    }
}

class Ping : Command() {
    override val description = "Pong!"
    override val example = "ping"

    init {
        subcommands += Pong()
    }

    override fun run(ctx: Context) {
        ctx.send("Pong!")
    }
}