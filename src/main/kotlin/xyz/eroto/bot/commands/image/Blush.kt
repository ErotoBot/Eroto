package xyz.eroto.bot.commands.image

import xyz.eroto.bot.entities.cmd.variant.WolkCommand
import xyz.eroto.bot.utils.WolkType

class Blush : WolkCommand() {
    override val type = WolkType.BLUSH
    override val description = ">/////<"
    override val example = "blush"
}