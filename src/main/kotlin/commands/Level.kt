package commands

import Command
import EmbedResponse
import Sendable
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import java.awt.Color
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt

class Level : Command() {
    override val name = "level"
    private val chars = "▏▎▍▌▋▊▉█"

    override fun process(args: List<String>, message: Message): Sendable {
        val embed = EmbedBuilder()
        val width = 30
        val user = if (message.mentionedMembers.isNotEmpty()) message.mentionedMembers[0].user else message.author
        val level = Points.getLevel(user.id)
        val xp = Points.levelToXp(level)

        val endLevelXp = Points.levelToXp(ceil(level))
        val proportion = level - level.toInt()
        val bars = proportion * width

        val fullBars = (bars * 8).toInt() / 8
        val partialBar = ((bars - fullBars) * 8).roundToInt()

        embed.addField("Level", level.toInt().toString(), true)
        embed.addField("Xp", "$xp / $endLevelXp", true)
        embed.addField("Progress", "`|${chars.last().toString().repeat(fullBars)}${chars[partialBar]}${" ".repeat((width - 1) - fullBars)}|`", false)
        embed.setColor(Color.getHSBColor(Random(user.idLong).nextFloat(), 0.8f, 0.8f))
        embed.setThumbnail(user.avatarUrl)
        return EmbedResponse(embed)
    }
}
