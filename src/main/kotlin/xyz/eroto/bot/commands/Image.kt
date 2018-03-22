package xyz.eroto.bot.commands

import okhttp3.Response
import org.json.JSONObject
import xyz.eroto.bot.Eroto
import xyz.eroto.bot.entities.cmd.Command
import xyz.eroto.bot.entities.cmd.Context
import xyz.eroto.bot.utils.Http

val headers = hashMapOf<>(
    'Content-Type' to 'application/json',
    'Authorization' to Eroto.config.weebshtoken,
    'User-Agent' to 'ErotoBot/1.0'
)

class Cry : Command() {
    override val description = "Cry"
    override val example = "cry"

    override fun run(ctx: Context) {
        val fut = Http.get("https://api.weeb.sh/images/random?type=cry", headers=headers)

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

class Hug : Command() {
    override val description = "Cry"
    override val example = "cry"

    override fun run(ctx: Context) {
        val fut = Http.get("https://api.weeb.sh/images/random?type=hug", headers=headers)

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

class Kiss : Command() {
    override val description = "Cry"
    override val example = "cry"

    override fun run(ctx: Context) {
        val fut = Http.get("https://api.weeb.sh/images/random?type=hug", headers=headers)

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