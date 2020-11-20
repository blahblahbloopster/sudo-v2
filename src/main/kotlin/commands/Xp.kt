package commands

import AdminCommand
import NullResponse
import Sendable
import TextResponse
import net.dv8tion.jda.api.entities.Message
import java.lang.NumberFormatException
import kotlin.math.max

class Xp : AdminCommand("xp") {
    override val help = "`xp @person <amount>` to award xp (can be negative), and `xp set @person <amount>` to set xp"

    override fun process(args: List<String>, message: Message): Sendable {
        val output = super.process(args, message)
        if (output !is NullResponse) {
            return output
        }
        message.member ?: return NullResponse()
        if (args.size == 1) return TextResponse("Mention a user and and an amount of xp to grant")
        if (args.size == 2) return TextResponse("Mention a user and and an amount of xp to grant")

        val xp = max(args.find { try { it.toLong(); true } catch (e: NumberFormatException) { false } }?.toLong() ?: return TextResponse("Specify an amount of xp to grant"), 0)
        val member = message.mentionedMembers.getOrNull(0) ?: return TextResponse("Mention a user")

        val newXp = if (args.contains("set")) {
            DB.setPoints(member, xp)
            xp
        } else {
            DB.incrementPoints(member, xp)
        }
        return TextResponse("${member.effectiveName} is now level ${DB.xpToLevel(newXp).toInt()} with $newXp xp")
    }
}
