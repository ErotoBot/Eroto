package xyz.eroto.bot.utils

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import xyz.eroto.bot.EventListener
import java.awt.Color
import java.util.concurrent.CompletableFuture

class ItemPicker(
        private val user: Member,
        private val confirm: Boolean = false,
        private val timeout: Long = 60000
) {

    private var index = 0
    private val embeds = mutableListOf<MessageEmbed>()

    private val rightEmote = "\u27A1"
    private val leftEmote = "\u2B05"
    private val confirmEmote = "\u2705"
    private val cancelEmote = "\u23F9"

    val items = mutableListOf<PickerItem>()
    var color: Color = Color.CYAN

    fun build(msg: Message) = build(msg.channel)

    fun build(channel: MessageChannel)
            = if (user.guild.selfMember.hasPermission(Permission.MESSAGE_ADD_REACTION)
            && user.guild.selfMember.hasPermission(Permission.MESSAGE_MANAGE)
            && user.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS)
            || user.guild.selfMember.hasPermission(Permission.ADMINISTRATOR)) buildReactions(channel) else buildInput(channel)

    private fun buildReactions(channel: MessageChannel): CompletableFuture<PickerItem> {
        val fut = CompletableFuture<PickerItem>()

        for (item in items) {
            val embed = EmbedBuilder().apply {
                setColor(item.color ?: color)
                setFooter(
                        "${if (item.footer != null) "${item.footer} | " else ""}Page ${items.indexOf(item) + 1}/${items.size}",
                        null
                )
                setAuthor(item.author, null, null)
                setTitle(item.title, item.url)

                if (item.description != null) {
                    descriptionBuilder.append(item.description)
                }

                setThumbnail(item.thumbnail)
                setImage(item.image)
            }

            embeds.add(embed.build())
        }

        channel.sendMessage(embeds[index]).queue { msg ->
            msg.addReaction(leftEmote).queue()
            if (confirm) {
                msg.addReaction(confirmEmote).queue()
            }
            msg.addReaction(cancelEmote).queue()
            msg.addReaction(rightEmote).queue()

            EventListener.waiter.await<MessageReactionAddEvent>(30, timeout) {
                if (it.messageId == msg.id && it.user.id == user.user.id) {
                    when (it.reactionEmote.name) {
                        leftEmote -> {
                            it.reaction.removeReaction(it.user).queue()
                            if (index - 1 >= 0) {
                                msg.editMessage(embeds[--index]).queue()
                            }
                        }

                        rightEmote -> {
                            it.reaction.removeReaction(it.user).queue()
                            if (index + 1 <= items.size - 1) {
                                msg.editMessage(embeds[++index]).queue()
                            }
                        }

                        confirmEmote -> {
                            if (confirm) {
                                msg.delete().queue()
                                fut.complete(items[index])
                            }
                        }

                        cancelEmote -> {
                            msg.delete().queue()
                        }
                    }
                    true
                } else {
                    false
                }
            }
        }

        return fut
    }

    private fun buildInput(channel: MessageChannel): CompletableFuture<PickerItem> {
        val fut = CompletableFuture<PickerItem>()
        val formatted = items.mapIndexed { i, item -> " ${i + 1}. ${item.title}" }.joinToString("\n")

        channel.sendMessage("Please choose an item from the list by sending its number:\n```\n$formatted```").queue { msg ->
            EventListener.waiter.await<MessageReceivedEvent>(1, timeout) {
                if (it.channel.id == msg.channel.id && it.author.id == user.user.id) {
                    when {
                        it.message.contentRaw.toLowerCase().startsWith("cancel") -> return@await true

                        it.message.contentRaw.toIntOrNull() == null -> {
                            msg.channel.sendMessage("Invalid number").queue()
                            return@await false
                        }

                        it.message.contentRaw.toInt() - 1 > items.size || it.message.contentRaw.toInt() - 1 < 0 -> {
                            msg.channel.sendMessage("Number out of bounds!")
                            return@await false
                        }

                        else -> {
                            index = it.message.contentRaw.toInt() - 1
                            msg.delete().queue()
                            fut.complete(items[index])
                        }
                    }

                    true
                } else {
                    false
                }
            }
        }

        return fut
    }

    data class PickerItem(
            val id: String,
            val title: String,
            val description: String? = null,
            val image: String? = null,
            val thumbnail: String? = null,
            val author: String? = null,
            val url: String? = null,
            val footer: String? = null,
            val color: Color? = null
    )
}