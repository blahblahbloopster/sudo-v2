package commands

import Command
import NullResponse
import Sendable
import TextResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import java.lang.NumberFormatException
import kotlin.math.max

class Xp : Command() {
    override val name = "xp"

    override fun process(args: List<String>, message: Message): Sendable {
        message.member ?: return NullResponse()
        if (!(message.member!!.hasPermission(Permission.ADMINISTRATOR) || message.author.id == Main.applicationInfo.owner.id)) return TextResponse("Insufficient permissions")
        if (args.size == 1) return TextResponse("Mention a user and and an amount of xp to grant")
        if (args.size == 2) return TextResponse("Mention a user and and an amount of xp to grant")

        val xp = max(args.find { try { it.toInt(); true } catch (e: NumberFormatException) { false } }?.toInt() ?: return TextResponse("Specify an amount of xp to grant"), 0)
        val member = message.mentionedMembers.getOrNull(0) ?: return TextResponse("Mention a user")

        val newXp = if (args.contains("set")) {
            DB.setPoints(member.id, xp)
            xp
        } else {
            DB.incrementPoints(member.id, xp)
        }
        return TextResponse("${member.effectiveName} is now level ${Points.getLevel(member.id)} with $newXp xp")
    }
}
