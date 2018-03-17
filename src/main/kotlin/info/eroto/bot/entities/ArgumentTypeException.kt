package info.eroto.bot.entities

class ArgumentTypeException(val input: String, val type: String) : Exception("$input is not of type $type")