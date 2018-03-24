package xyz.eroto.bot.commands.`fun`

import xyz.eroto.bot.entities.cmd.Command
import xyz.eroto.bot.entities.cmd.Context
import xyz.eroto.bot.entities.cmd.argument
import java.util.*

class Choose : Command() {
    override val description = "Let me choose between options!"
    override val example = "choose a, b, c, def"

    private val random = Random()

    init {
        arguments += argument<Array<String>>("choices")
    }

    override fun run(ctx: Context) {
        val opts = ctx.args["choices"] as Array<String>
        val item = opts[random.nextInt(opts.size)]
        ctx.send(item)
    }

}