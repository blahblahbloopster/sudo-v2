package commands

import Command
import Sendable
import TextResponse
import net.dv8tion.jda.api.entities.Message

class Ping : Command() {
    override val name = "ping"
    override val help = "Returns the bot's discord gateway ping time in milliseconds"

    override fun process(args: List<String>, message: Message): Sendable {
        return TextResponse("My ping is ${Main.jda.gatewayPing}ms")
    }
}
