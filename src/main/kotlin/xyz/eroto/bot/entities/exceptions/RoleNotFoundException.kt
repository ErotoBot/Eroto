package xyz.eroto.bot.entities.exceptions

class RoleNotFoundException(val input: String) : Exception("No roles found for $input")