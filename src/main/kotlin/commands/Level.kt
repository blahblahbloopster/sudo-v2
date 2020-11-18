package commands

import Command
import EmbedResponse
import Sendable
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message

class Level : Command() {
    override val name = "level"
    private val chars = "▏▎▍▌▋▊▉█"

    override fun process(args: List<String>, message: Message): Sendable {
        val embed = EmbedBuilder()
        val width = 30
        val level = Points.getLevel(message.author.id)

        val fullBars = level.toInt() * width
        val proportion = level - fullBars

        embed.addField("Progress", "`|${chars.last().toString().repeat(fullBars)}${chars[(proportion * 8).toInt() / 8]}${" ".repeat((width - 1) - fullBars)}|`", false)
        return EmbedResponse(embed)
    }
}
