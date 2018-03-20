package xyz.eroto.bot.entities.exceptions

import net.dv8tion.jda.core.Permission

class MemberMissingPermissionException(val perm: Permission) : Exception("Missing required permission: ${perm.getName()}")