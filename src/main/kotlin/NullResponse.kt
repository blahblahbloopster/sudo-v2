import net.dv8tion.jda.api.entities.TextChannel

class NullResponse : Sendable {
    override fun send(channel: TextChannel) {}
}