package xyz.eroto.bot.entities

abstract class Command(val name: String? = null) {
    abstract val description: String
    abstract val example: String

    val category = Category.OTHER
    val subcommands = mutableListOf<Subcommand>()
    val arguments = mutableListOf<Argument<*>>()
    val aliases = mutableListOf<String>()

    abstract fun run(ctx: Context)
}