import net.dv8tion.jda.api.entities.TextChannel

interface Sendable {
    fun send(channel: TextChannel)
}
