import net.dv8tion.jda.api.entities.TextChannel

open class TextResponse(private val message: String) : Sendable {

    override fun send(channel: TextChannel) {
        channel.sendMessage(message).queue()
    }
}
