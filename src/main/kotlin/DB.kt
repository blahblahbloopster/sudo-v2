import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.math.max
import kotlin.math.pow

object DB {
    init {
        Database.connect("jdbc:sqlite:users.db")
    }

    fun getPoints(member: Member): Long {
        var points: Long? = null
        transaction {
            for (user in Users.select { Users.id.eq(member.user.id) and Users.serverId.eq(member.guild.id) }) {
                points = user[Users.points]
                break
            }
            points ?: {
                Users.insert {
                    it[Users.id] = member.user.id
                    it[Users.points] = 0L
                    it[Users.serverId] = member.guild.id
                }
            }
        }
        return points ?: 0L
    }

    fun setPoints(member: Member, xp: Long) {
        transaction {
            Users.update( {Users.id.eq(member.user.id) and Users.serverId.eq(member.guild.id)} ) {
                it[Users.points] = max(xp, 0L)
            }
            Users.insert {
                it[Users.id] = member.user.id
                it[Users.points] = max(xp, 0L)
                it[Users.serverId] = member.guild.id
            }
        }
    }

    fun incrementPoints(member: Member, diff: Long): Long {
        var xp = max(diff, 0L)
        transaction {
            for (user in Users.select { Users.id.eq(member.user.id) and Users.serverId.eq(member.guild.id) }) {
                xp = max(user[Users.points] + diff, 0L)
                Users.update( {Users.id.eq(member.user.id) and Users.serverId.eq(member.guild.id)} ) {
                    it[Users.points] = xp
                }
                return@transaction
            }
            Users.insert {
                it[Users.id] = member.user.id
                it[Users.points] = max(diff, 0L)
                it[Users.serverId] = member.guild.id
            }
        }
        return xp
    }

    fun getPrefix(guild: Guild): String {
        var prefix = ""
        transaction {
            for (server in Servers.select { Servers.id.eq(guild.id) }) {
                prefix = server[Servers.prefix]
                break
            }
        }
        return prefix
    }

    private const val messageSendXp = 5L

    fun messageSent(author: Member, channel: TextChannel) {
        if (author.user == Main.jda.selfUser) return
        var startLevel = 0
        var endLevel = 0
        transaction {
            var exists = false
            for (user in Users.select { Users.id.eq(author.user.id) and Users.serverId.eq(author.guild.id) }) {
                startLevel = xpToLevel(user[Users.points]).toInt()
                val xp = max(user[Users.points] + messageSendXp, 0)
                Users.update( {Users.id.eq(author.user.id) and Users.serverId.eq(author.guild.id)} ) {
                    it[Users.points] = xp
                }
                endLevel = xpToLevel(xp).toInt()
                exists = true
                break
            }
            if (!exists) {
                Users.insert {
                    startLevel = 0
                    endLevel = 0
                    it[Users.id] = author.user.id
                    it[Users.points] = max(messageSendXp, 0L)
                    it[Users.serverId] = author.guild.id
                }
            }
        }

        if (startLevel < endLevel) {
            TextResponse("${author.asMention} has leveled up to lvl $endLevel").send(channel)
        }
    }

    fun getLevel(member: Member): Double {
        val xp = getPoints(member)
        return xpToLevel(xp)
    }

    fun xpToLevel(xp: Long): Double {
        return xp.toDouble().pow(0.25)
    }

    fun levelToXp(level: Double): Long {
        return level.pow(4).toLong()
    }

    fun printUsers() {
        transaction {
            for (user in Users.selectAll()) {
                println("${user[Users.id]} in ${user[Users.serverId]}: ${user[Users.points]}")
            }
        }
    }
}

fun main() {
    // THIS WILL CLEAR THE TABLE
    Database.connect("jdbc:sqlite:users.db")
    transaction {
        SchemaUtils.drop(Users, Servers)
        SchemaUtils.create(Users, Servers)

        Servers.insert {
            it[id] = "709560490199744593"
            it[hallOfFameEnabled] = true
            it[hallOfFameThreshold] = 3
            it[hallOfFameEmoji] = ":linuxPowered:"
            it[hallOfFameChannel] = "716131138468446348"
            it[prefix] = ";"
        }
    }
}

object Users : Table() {
    val id = varchar("id", 25)
    val points = long("points")
    val serverId = varchar("serverId", 25)
}

object Servers : Table() {
    val id = varchar("id", 25)
    val hallOfFameEnabled = bool("hallOfFameEnabled")
    val hallOfFameThreshold = integer("hallOfFameThreshold")
    val hallOfFameEmoji = text("hallOfFameEmoji")
    val hallOfFameChannel = text("hallOfFameChannel")
    val prefix = varchar("prefix", 10)
}
