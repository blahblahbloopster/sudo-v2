package commands

import Command
import EmbedResponse
import Sendable
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

class Level : Command() {
    override val name = "level"
    private val chars = "▏▎▍▌▋▊▉█"
    override val help = "Gets your (or a mentioned member)'s level"

    override fun process(args: List<String>, message: Message): Sendable {
        val embed = EmbedBuilder()
        val width = 30
        val member = if (message.mentionedMembers.isNotEmpty()) message.mentionedMembers[0] else message.member!!
        val level = DB.getLevel(member)
        val xp = DB.levelToXp(level)

        val endLevelXp = DB.levelToXp(ceil(level))
        val proportion = level - level.toInt()
        val bars = proportion * width

        val fullBars = (bars * 8).toInt() / 8
        val partialBar = min(((bars - fullBars) * 8).roundToInt(), 7)

        embed.addField("Level", level.toInt().toString(), true)
        embed.addField("Xp", "$xp / $endLevelXp", true)
        embed.addField("Progress", "`|${chars.last().toString().repeat(fullBars)}${chars[partialBar]}${" ".repeat((width - 1) - fullBars)}|`", false)
        embed.setColor(member.color)
        embed.setThumbnail(member.user.avatarUrl)
        return EmbedResponse(embed)
    }
}
