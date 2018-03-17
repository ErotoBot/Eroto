package info.eroto.bot.cogs

import info.eroto.bot.annotations.Command
import info.eroto.bot.entities.Cog
import info.eroto.bot.entities.Context

class Test : Cog() {
    @Command
    fun testInt(ctx: Context, number: Int) {
        ctx.send((number * 6).toString())
    }

    @Command
    fun testBoolean(ctx: Context, bool: Boolean) {
        ctx.send(if (bool) "yes" else "no")
    }

    @Command
    fun testStringArray(ctx: Context, arr: Array<String>) {
        ctx.send(arr.joinToString("+"))
    }
}