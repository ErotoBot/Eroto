package xyz.eroto.bot.commands.image

import xyz.eroto.bot.entities.cmd.variant.WolkCommand
import xyz.eroto.bot.utils.WolkType

class Lewd : WolkCommand() {
    override val type = WolkType.LEWD
    override val description = "Scandalous!"
    override val example = "lewd"
}