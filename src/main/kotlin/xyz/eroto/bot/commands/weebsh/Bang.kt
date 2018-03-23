package xyz.eroto.bot.commands.weebsh

import xyz.eroto.bot.entities.cmd.variant.WolkCommand
import xyz.eroto.bot.utils.WolkType

class Bang : WolkCommand() {
    override val type = WolkType.BANG
    override val description = "Bang!"
    override val example = "bang"
}