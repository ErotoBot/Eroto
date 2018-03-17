package info.eroto.bot.entities

import java.lang.reflect.Method

class CommandClass(private val klass: Any, private val method: Method) {
    private val parameters = method.parameters.slice(1 until method.parameters.size)

    fun run(ctx: Context) {
        val args = mutableListOf<Any>(ctx)

        for (par in parameters) {
            val index = parameters.indexOf(par)

            val arg = ctx.args[index]

            args += when (par.type) {
                Int::class.java -> arg.toInt()
                Long::class.java -> arg.toLong()
                Float::class.java -> arg.toFloat()
                String::class.java -> arg
                Boolean::class.java -> Converter.boolean(arg)
                Array<Int>::class.java -> arg.split("\\s?,\\s?".toRegex()).map { it.toInt() }.toTypedArray()
                Array<Long>::class.java -> arg.split("\\s?,\\s?".toRegex()).map { it.toLong() }.toTypedArray()
                Array<Float>::class.java -> arg.split("\\s?,\\s?".toRegex()).map { it.toFloat() }.toTypedArray()
                Array<String>::class.java -> arg.split("\\s?,\\s?".toRegex()).toTypedArray()
                Array<Boolean>::class.java -> arg.split("\\s?,\\s?".toRegex()).map { Converter.boolean(it) }.toTypedArray()

                else -> throw UnsupportedTypeException(par.type.typeName)
            }
        }

        method.invoke(klass, *args.toTypedArray())
    }
}