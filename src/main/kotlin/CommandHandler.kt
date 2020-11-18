import java.lang.Exception

class CommandHandler {
    val commands = mutableListOf<Command>()

    fun process(input: String, prefix: String): Sendable {
        if (!input.startsWith(prefix)) {
            return NullResponse()
        }
        val inp = input.removePrefix(prefix)

        val command = commands.find { cmd -> cmd.matches(inp) } ?: return ErrorResponse("Command not found")
        val args = inp.split(" ")  // todo: make this allow for arguments in quotes to have spaces

        try {
            return command.process(args)
        } catch (error: Exception) {
            return ErrorResponse(error.stackTraceToString())
        }
    }
}
