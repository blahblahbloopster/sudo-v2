import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message

abstract class AdminCommand(override val name: String) : Command() {

    override fun process(args: List<String>, message: Message): Sendable {
        message.member ?: return ErrorResponse("Not a member of a server")
        if (!(message.member!!.hasPermission(Permission.ADMINISTRATOR) || message.author.id == Main.applicationInfo.owner.id)) return ErrorResponse("Insufficient permissions")
        return NullResponse()
    }
}
