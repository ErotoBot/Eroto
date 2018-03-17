package info.eroto.bot.entities

import info.eroto.bot.annotations.Subcommand
import java.lang.reflect.Method

class CommandClass(val name: String, private val klass: Any, private val method: Method) {
    private val parameters = method.parameters.slice(1 until method.parameters.size)

    val subcommands = mutableMapOf<String, CommandClass>()
    val category = klass::class.java.simpleName

    init {
        klass::class.java.methods.forEach { m ->
            m.annotations.filterIsInstance<Subcommand>().filter { it.root == name }.forEach { ann ->
                val name = if (ann.name.isBlank()) m.name else ann.name
                val cmd = CommandClass(name, klass, m)

                subcommands[name] = cmd
            }
        }
    }

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