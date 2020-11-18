import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import kotlin.math.pow

object Points {
    private const val messageSendXp = 5

    fun messageSent(author: User, channel: TextChannel) {
        if (author == Main.jda.selfUser) return
        val startLevel = getLevel(author.id).toInt()
        DB.incrementPoints(author.id, messageSendXp)
        val endLevel = getLevel(author.id).toInt()

        if (startLevel < endLevel) {
            TextResponse("${author.asMention} up to lvl $endLevel").send(channel)
        }
    }

    fun getLevel(id: String): Double {
        val xp = DB.getPoints(id)
        return xpToLevel(xp)
    }

    fun xpToLevel(xp: Int): Double {
        return xp.toDouble().pow(0.25)
    }

    fun levelToXp(level: Double): Int {
        return level.pow(4).toInt()
    }
}
