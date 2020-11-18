package commands

import Command
import Sendable
import TextResponse
import net.dv8tion.jda.api.entities.Message

class Help : Command() {
    override val name = "help"

    override fun process(args: List<String>, message: Message): Sendable {
        return TextResponse("Hi!")
    }
}
