package xyz.eroto.bot.commands.`fun`

import okhttp3.Response
import org.json.JSONObject
import xyz.eroto.bot.entities.cmd.Command
import xyz.eroto.bot.entities.cmd.Context
import xyz.eroto.bot.utils.Http

class Cat : Command() {
    override val description = "Sends you a random cat"
    override val example = "cat"

    override fun run(ctx: Context) {
        val fut = Http.get("https://aws.random.cat/meow")

        fut.exceptionally {
            ctx.send(it.toString())

            Response.Builder().build()
        }

        fut.thenAccept { res ->
            val json = JSONObject(res.body()!!.string())

            ctx.send(json.getString("file"))

            res.close()
        }
    }
}