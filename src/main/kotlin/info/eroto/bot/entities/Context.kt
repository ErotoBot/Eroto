package info.eroto.bot.entities

class Context(val args: List<String>) {
    fun send(vararg args: String) {
        println(args.joinToString(" "))
    }
}