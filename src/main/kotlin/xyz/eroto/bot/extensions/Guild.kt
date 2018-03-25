package xyz.eroto.bot.extensions

import net.dv8tion.jda.core.entities.Guild

fun Guild.searchMembers(query: String) = members.filter {
    query in "<@!${it.user.id}> ${it.asMention} ${it.user.name}#${it.user.discriminator} ${it.nickname ?: ""} ${it.user.id}"
}

fun Guild.searchRole(query: String) = roles.filter {
    query in "${it.asMention} ${it.name} @${it.name} ${it.id}"
}

fun Guild.searchTextChannel(query: String) = textChannels.filter {
    query in "${it.asMention} #${it.name} ${it.id}"
}