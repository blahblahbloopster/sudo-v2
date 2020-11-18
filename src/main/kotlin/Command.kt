abstract class Command {
    private val name = ""

    abstract fun process(args: List<String>): Sendable

    fun matches(inp: String): Boolean {
        return inp.startsWith(name)
    }
}
