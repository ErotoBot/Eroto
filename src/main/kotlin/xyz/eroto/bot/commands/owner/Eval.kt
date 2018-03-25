package xyz.eroto.bot.commands.owner

import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import xyz.eroto.bot.entities.cmd.Command
import xyz.eroto.bot.entities.cmd.Context
import xyz.eroto.bot.entities.cmd.argument
import javax.script.ScriptEngineManager
import javax.script.ScriptException

class Eval : Command() {
    override val description = "Evaluate code"
    override val example = "eval println(\"memes\")"

    init {
        ownerOnly = true

        arguments += argument<String>("code")
    }

    override fun run(ctx: Context) {
        val code = ctx.msg.contentRaw.split("eval")[1].trim()

        var text = """
            |import xyz.eroto.bot.Eroto
            |import xyz.eroto.bot.EventListener
            |import xyz.eroto.bot.CommandManager
            |import xyz.eroto.bot.utils.Http
            |
            |val ctx = bindings["ctx"] as xyz.eroto.bot.entities.cmd.Context
            |
            |$code
        """.trimMargin()

        if ("import" in ctx.flags || "i" in ctx.flags) {
            val toImport = ctx.flags["import"] ?: ctx.flags["i"]

            if (toImport != null) {
                text = "$toImport\n$text"
            }
        }

        val engine = KotlinJsr223JvmLocalScriptEngineFactory().scriptEngine

        engine.put("ctx", ctx)

        try {
            val res = engine.eval(text)

            ctx.send("```kotlin\n$res```")
        } catch (e: Exception) {
            ctx.sendException(e)
        }
    }
}