package xyz.eroto.bot.entities.exceptions

import net.dv8tion.jda.core.Permission

class BotMissingPermissionException(val perm: Permission) : Exception("Bot missing required permission: ${perm.getName()}")