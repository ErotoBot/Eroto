package xyz.eroto.bot.cogs

import xyz.eroto.bot.annotations.Command
import xyz.eroto.bot.entities.Cog
import xyz.eroto.bot.entities.Context
import xyz.eroto.bot.entities.ICommand
import net.dv8tion.jda.core.EmbedBuilder

class Info : Cog() {
    @Command
    class About : ICommand {
        override fun run(ctx: Context) {
            val eb = EmbedBuilder()
            eb.setTitle("About me")
            eb.setDescription("I'm a bot made in Kotlin here to help you out!")
            eb.addField("Source code", "[Click me!](https://github.com/Erotobot/Eroto)", true)
            eb.addField("Developers", "Martmists#3740\nnoud02#0080\nRayintu#0001\nHappynu#5075", true)
            eb.setThumbnail(ctx.event.jda.selfUser.avatarUrl)
            ctx.event.channel.sendMessage(eb.build()).queue()
        }
    }

    @Command
    class Stats: ICommand {
        override fun run(ctx: Context) {
            val eb = EmbedBuilder()
            val jda = ctx.event.jda
            eb.setTitle("Stats", null)
            eb.setDescription("Stats about how I'm doing!")
            eb.addField("Servers", jda.guilds.size.toString(), true)
            eb.addField("Channels", (jda.textChannels.size + jda.voiceChannels.size).toString(), true)
            eb.addField("Users", jda.users.size.toString(), true)
            eb.setThumbnail(ctx.event.jda.selfUser.avatarUrl)
            ctx.event.channel.sendMessage(eb.build()).queue()
        }
    }
}