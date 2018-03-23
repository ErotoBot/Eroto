package xyz.eroto.bot.entities.cmd.variant

import net.dv8tion.jda.core.EmbedBuilder
import xyz.eroto.bot.Eroto
import xyz.eroto.bot.entities.cmd.Context
import xyz.eroto.bot.entities.cmd.Subcommand
import xyz.eroto.bot.utils.WolkType
import java.awt.Color

abstract class WolkSubcommand(name: String? = null) : Subcommand(name) {
    abstract val type: WolkType

    open var color: Color = Color(250, 239, 211)
    open fun title(ctx: Context): String? = null

    override fun run(ctx: Context) {
        val fut = Eroto.wolk.getByType(type)

        fut.thenAccept { res ->
            ctx.send(EmbedBuilder().apply {
                setColor(color)
                setImage(res.url)
                setTitle(title(ctx))
                setFooter("Powered by weeb.sh", null)
            }.build())
        }

        fut.exceptionally {
            ctx.send(it.toString())

            null
        }
    }
}