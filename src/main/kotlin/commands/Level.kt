package commands

import Command
import Sendable
import TextResponse
import net.dv8tion.jda.api.entities.Message

class Level : Command() {
    override val name = "level"

    override fun process(args: List<String>, message: Message): Sendable {
        return TextResponse("You are level ${Points.getLevel(message.author.id)}")
    }
}
