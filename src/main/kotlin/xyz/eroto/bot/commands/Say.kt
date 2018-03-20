package xyz.eroto.bot.commands

import xyz.eroto.bot.entities.cmd.Command
import xyz.eroto.bot.entities.cmd.Context
import xyz.eroto.bot.entities.cmd.argument

class Say : Command() {
    override val description = "Have me say something!"
    override val example = "say memes"

    init {
        arguments += argument("content", defaultValue = "Hello world!")
    }

    override fun run(ctx: Context) {
        val msg = ctx.args["content"] as String

        ctx.send(msg)
    }
}