package info.eroto.bot.entities

class MissingArgumentException(val arg: String) : Exception("Missing argument $arg")