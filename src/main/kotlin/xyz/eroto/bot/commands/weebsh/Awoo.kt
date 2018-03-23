package xyz.eroto.bot.commands.weebsh

import xyz.eroto.bot.entities.cmd.variant.WolkCommand
import xyz.eroto.bot.utils.WolkType

class Awoo : WolkCommand() {
    override val type = WolkType.AWOO
    override val description = "awoo~"
    override val example = "awoo"
}