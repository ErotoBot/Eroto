package xyz.eroto.bot.commands.image

import xyz.eroto.bot.entities.cmd.variant.WolkCommand
import xyz.eroto.bot.utils.WolkType


class Cry : WolkCommand() {
    override val type = WolkType.CRY
    override val description = "Cry"
    override val example = "cry"
}