package info.eroto.bot.cogs

import info.eroto.bot.annotations.Command
import info.eroto.bot.entities.Cog
import info.eroto.bot.entities.Context
import info.eroto.bot.entities.ICommand
import net.dv8tion.jda.core.EmbedBuilder

class Info : Cog() {
    @Command
    class About : ICommand {
        override fun run(ctx: Context) {
            val eb = EmbedBuilder()
            eb.setTitle("About me", null)
            eb.setDescription("I'm a bot made in Kotlin here to help you out!")
            eb.addField("Source code", "[Click me!](https://github.com/Erotobot/Eroto)", true)
            eb.addField("Developers", "Martmists#3740\nnoud02#0080\nRayintu#0001\nHappynu#5075", true)
            eb.setThumbnail(ctx.event.jda.selfUser.avatarUrl)
            ctx.event.channel.sendMessage(eb.build()).queue()
        }
    }
}