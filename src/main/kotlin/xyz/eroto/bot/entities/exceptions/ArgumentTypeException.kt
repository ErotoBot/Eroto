package xyz.eroto.bot.entities.exceptions

import kotlin.reflect.KClass

class ArgumentTypeException(
        val name: String,
        val input: String,
        val type: KClass<*>
) : Exception("Argument $name doesn't match type $type (input: $input)")