package xyz.eroto.bot.commands.image

import xyz.eroto.bot.entities.cmd.variant.WolkCommand
import xyz.eroto.bot.utils.WolkType

class Dance : WolkCommand() {
    override val type = WolkType.DANCE
    override val description = "We like to party! We like, we like to party!"
    override val example = "dance"
}