package info.eroto.bot.annotations

import kotlin.reflect.KClass

annotation class Argument(val name: String, val display: String, val type: KClass<*> = String::class, val default: String = "")