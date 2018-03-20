package xyz.eroto.bot.entities

import net.dv8tion.jda.core.Permission

data class BotPermission(val perm: Permission, val optional: Boolean = false)