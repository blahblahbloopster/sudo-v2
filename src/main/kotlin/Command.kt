import net.dv8tion.jda.api.entities.Message

abstract class Command {
    abstract val name: String
    abstract val help: String

    abstract fun process(args: List<String>, message: Message): Sendable

    fun matches(inp: String): Boolean {
        return inp.startsWith(name)
    }
}
