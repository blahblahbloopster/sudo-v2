package commands

import Command
import EmbedResponse
import Sendable
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message

class Help : Command() {
    override val name = "help"
    override val help = "Gets a list of commands and usage information"

    override fun process(args: List<String>, message: Message): Sendable {
        val embed = EmbedBuilder()
        for (command in Main.handler.commands) {
            embed.addField(command.name, command.help, true)
        }
        return EmbedResponse(embed)
    }
}
