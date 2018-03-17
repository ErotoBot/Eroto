package info.eroto.bot

import info.eroto.bot.entities.Context

fun main(args: Array<String>) {
    val cogLoader = CogLoader()

    val testInt = CogLoader.commands["testInt"]!!
    val testBoolean = CogLoader.commands["testBoolean"]!!
    val testStringArray = CogLoader.commands["testStringArray"]!!

    testInt.run(Context(listOf("1")))
    testBoolean.run(Context(listOf("true")))
    testStringArray.run(Context(listOf("a,b,c")))
}