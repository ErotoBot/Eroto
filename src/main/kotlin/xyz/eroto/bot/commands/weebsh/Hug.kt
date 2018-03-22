package xyz.eroto.bot.commands.weebsh

import net.dv8tion.jda.core.entities.Member
import okhttp3.Response
import org.json.JSONObject
import xyz.eroto.bot.Eroto
import xyz.eroto.bot.entities.cmd.Command
import xyz.eroto.bot.entities.cmd.Context
import xyz.eroto.bot.entities.cmd.argument
import xyz.eroto.bot.utils.Http

private val headers = hashMapOf(
        "Content-Type" to "application/json",
        "Authorization" to Eroto.config.weebshtoken,
        "User-Agent" to "ErotoBot/1.0"
)

class Hug : Command() {
    override val description = "hug a user"
    override val example = "hug @user"

    init {
        arguments += argument<Member>("user")
    }

    override fun run(ctx: Context) {
        val fut = Http.get("https://api.weeb.sh/images/random?type=hug", { headers.entries.map { header(it.key, it.value) } })

        fut.exceptionally {
            ctx.send(it.toString())

            Response.Builder().build()
        }

        fut.thenAccept { res ->
            val json = JSONObject(res.body()!!.string())

            val target = ctx.args["user"] as Member

            val msg = "${target.asMention}, you got a hug from ${ctx.author.asMention}"

            ctx.send(json.getString("url"))

            res.close()
        }
    }
}