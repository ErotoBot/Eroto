package xyz.eroto.bot.entities.cmd

abstract class Command(val name: String? = null) {
    abstract val description: String
    abstract val example: String

    open var guildOnly = false
    open var ownerOnly = false
    open var category = Category.OTHER
    
    val subcommands = mutableListOf<Subcommand>()
    val botPermissions = mutableListOf<BotPermission>()
    val permissions = mutableListOf<MemberPermission>()
    val arguments = mutableListOf<Argument<*>>()
    val aliases = mutableListOf<String>()

    abstract fun run(ctx: Context)
}