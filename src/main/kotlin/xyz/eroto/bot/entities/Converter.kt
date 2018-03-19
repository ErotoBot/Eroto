package xyz.eroto.bot.entities

object Converter {
    fun boolean(input: String): Boolean {
        val trueList = listOf(
                "on",
                "yes",
                "true",
                "enable",
                "enabled"
        )
        val falseList = listOf(
                "off",
                "no",
                "false",
                "disable",
                "disabled"
        )

        if (input.toLowerCase() !in trueList && input.toLowerCase() !in falseList)
            throw ArgumentTypeException(input, "boolean")

        return input in trueList
    }
}