import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.TextChannel

class EmbedResponse(private val embed: EmbedBuilder) : Sendable {

    override fun send(channel: TextChannel) {
        channel.sendMessage(embed.build()).queue()
    }
}
