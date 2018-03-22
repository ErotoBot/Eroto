package xyz.eroto.bot.utils

import org.json.JSONObject
import xyz.eroto.bot.Eroto
import java.util.concurrent.CompletableFuture
import kotlin.reflect.jvm.jvmName

enum class WolkType(val str: String) {
    AWOO("awoo"),
    BANG("bang"),
    BLUSH("blush"),
    CLAGWIMOTH("clagwimoth"),
    CRY("cry"),
    CUDDLE("cuddle"),
    DANCE("dance"),
    HUG("hug"),
    INSULT("insult"),
    JOJO("jojo"),
    KISS("kiss"),
    LEWD("lewd"),
    LICK("lick"),
    MEGUMIN("megumin"),
    NEKO("neko"),
    NOM("nom"),
    OWO("owo"),
    PAT("pat"),
    POKE("poke"),
    POUT("pout"),
    REM("rem"),
    SHRUG("shrug"),
    SLAP("slap"),
    SLEEPY("sleepy"),
    SMILE("smile"),
    TEEHEE("teehee"),
    SMUG("smug"),
    STARE("stare"),
    THUMBSUP("thumbsup"),
    TRIGGERED("triggered"),
    WAG("wag"),
    WAIFU_INSULT("waifu_insult"),
    WASTED("wasted"),
    SUMFUK("sumfuk"),
    DAB("dab"),
    TICKLE("tickle"),
    HIGHFIVE("highfive"),
    BANGHEAD("banghead"),
    BITE("bite"),
    DISCORD_MEMES("discord_memes"),
    NANI("nani"),
    INITIAL_D("initial_d"),
    DELET_THIS("delet_this"),
    POI("poi"),
    THINKING("thinking"),
    GREET("greet")
}

data class WolkTag(
        val name: String,
        val hidden: Boolean,
        val user: String
)

data class WolkResponse(
        val id: String,
        val baseType: String,
        val fileType: String,
        val mimeType: String,
        val account: String,
        val hidden: Boolean,
        val nsfw: Boolean,
        val tags: List<WolkTag>,
        val url: String
)

class Wolk {
    fun getByType(type: WolkType) = getByType(type.str)

    fun getByType(type: String): CompletableFuture<WolkResponse> {
        val fut = CompletableFuture<WolkResponse>()

        val futt = Http.get("https://api.weeb.sh/images/random?type=$type") {
            header("User-Agent", "Eroto (https://github.com/ErotoBot/Eroto)")
            header("Authorization", "Wolke ${Eroto.config.api.weebsh}")
        }

        futt.thenAccept { res ->
            val json = JSONObject(res.body()!!.string())
            val tags = mutableListOf<WolkTag>()

            if (res.code() != 200) {
                throw Exception("Expected status code 200, got ${res.code()}")
            }

            (0 until json.getJSONArray("tags").length())
                    .map {
                        json
                                .getJSONArray("tags")
                                .getJSONObject(it)
                    }
                    .forEach {
                        tags += WolkTag(
                                it.getString("name"),
                                it.getBoolean("hidden"),
                                it.getString("user")
                        )
                    }

            res.close()

            fut.complete(
                    WolkResponse(
                            json.getString("id"),
                            json.getString("baseType"),
                            json.getString("fileType"),
                            json.getString("mimeType"),
                            json.getString("account"),
                            json.getBoolean("hidden"),
                            json.getBoolean("nsfw"),
                            tags,
                            json.getString("url")
                    )
            )
        }

        futt.exceptionally {
            fut.completeExceptionally(it)

            null
        }

        return fut
    }
}