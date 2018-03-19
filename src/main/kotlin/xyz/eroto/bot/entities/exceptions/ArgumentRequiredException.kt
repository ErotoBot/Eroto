package xyz.eroto.bot.entities.exceptions

class ArgumentRequiredException(val name: String) : Exception("Argument $name is required")