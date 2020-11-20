import net.dv8tion.jda.api.entities.Message
import java.lang.Exception

class CommandHandler {
    val commands = mutableListOf<Command>()

    fun process(input: Message, prefix: String): Sendable {
        val messageText = input.contentRaw
        if (!messageText.startsWith(prefix)) {
            return NullResponse()
        }
        val inp = messageText.removePrefix(prefix)

        val command = commands.find { cmd -> cmd.matches(inp) } ?: return ErrorResponse("Command not found")
        val args = inp.split(" ")  // todo: make this allow for arguments in quotes to have spaces

        try {
            return command.process(args, input)
        } catch (error: Exception) {
            println(input)
            error.printStackTrace()
            return ErrorResponse(error.stackTraceToString())
        }
    }
}
