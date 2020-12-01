import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.math.max
import kotlin.math.pow

object DB {
    val settings = mutableListOf<ServerSetting<*>>()
    init {
        val trueStrings = listOf("true", "1", "yes", "y")
        val falseStrings = listOf("false", "0", "no", "n")
        fun greaterThan(a: Int?, b: Int?): Boolean {
            a ?: return false
            b ?: return false
            return a > b
        }
        Database.connect("jdbc:sqlite:users.db")
        settings.add(ServerSetting(Servers.hallOfFameEmoji, "hallOfFameEmoji", { inp, guild -> guild.getEmotesByName(inp, true).isNotEmpty() }, { inp, _ -> inp }))
        settings.add(ServerSetting(Servers.hallOfFameThreshold, "hallOfFameThreshold", { inp, _ -> greaterThan(inp.toIntOrNull(), 0) }, { inp, _ -> inp.toInt() }))
        settings.add(ServerSetting(Servers.hallOfFameEnabled, "hallOfFameEnabled", { inp, _ -> trueStrings.contains(inp.toLowerCase()) || falseStrings.contains(inp.toLowerCase()) }, { inp, _ -> trueStrings.contains(inp.toLowerCase()) }))
        settings.add(ServerSetting(Servers.hallOfFameChannel, "hallOfFameChannel", { inp, guild -> guild.getTextChannelById(inp.replace("<#", "").replace(">", "")) != null }, { inp, _ -> inp.replace("<#", "").replace(">", "") }))
        settings.add(ServerSetting(Servers.levelChannel, "levelChannel", { inp, guild -> guild.getTextChannelById(inp.replace("<#", "").replace(">", "")) != null }, { inp, _ -> inp.replace("<#", "").replace(">", "") }))
        settings.add(ServerSetting(Servers.prefix, "prefix", { inp, _ -> inp.length < 5 }, { inp, _ -> inp }))
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
        return getField(Servers.prefix, guild)?: return "!"
    }

    fun getHallOfFameChannel(guild: Guild): TextChannel? {
        return guild.getTextChannelById(getField(Servers.hallOfFameChannel, guild) ?: return null)
    }

    fun getHallOfFameEnabled(guild: Guild): Boolean {
        return getField(Servers.hallOfFameEnabled, guild) ?: return false
    }

    fun getHallOfFameThreshold(guild: Guild): Int {
        return getField(Servers.hallOfFameThreshold, guild) ?: return 5
    }

    fun getHallOfFameEmoji(guild: Guild): String? {
        return getField(Servers.hallOfFameEmoji, guild)
    }

    fun getLevelChannel(guild: Guild): TextChannel? {
        return guild.getTextChannelById(getField(Servers.levelChannel, guild) ?: return null)
    }

    fun <T>getField(field: Column<T>, guild: Guild): T? {
        var output: T? = null
        transaction {
            for (server in Servers.select { Servers.id.eq(guild.id) }) {
                output = server[field]
                break
            }
        }
        return output
    }

    fun <T>setField(field: Column<T>, guild: Guild, value: T) {
        transaction {
            Servers.update({ Servers.id.eq(guild.id) }) {
                it[field] = value
            }
        }
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
            val levelChannel = getLevelChannel(channel.guild) ?: channel
            TextResponse("${author.asMention} has leveled up to lvl $endLevel").send(levelChannel)
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

    fun getServerSettings(): List<Column<*>> {
        return settings.map { it.column }
    }

    fun getAllSettings(guild: Guild): List<Pair<ServerSetting<*>, *>> {
        val settings = mutableListOf<Pair<ServerSetting<*>, *>>()
        transaction {
            for (server in Servers.select { Servers.id.eq(guild.id) }) {
                for (setting in DB.settings) {
                    settings.add(Pair(setting, server[setting.column]))
                }
            }
        }
        return settings.toList()
    }
}

class ServerSetting<T>(val column: Column<T>, val name: String, private val validator: (inp: String, guild: Guild) -> Boolean, private val converter: (inp: String, guild: Guild) -> T) {

    fun get(guild: Guild): T? {
        return DB.getField(column, guild)
    }

    fun set(value: String, guild: Guild) {
        if (validator(value, guild)) {
            DB.setField(column, guild, converter(value, guild))
        } else {
            throw RuntimeException("Invalid value \"$value\" for setting \"$name\"")
        }
    }
}

fun main() {
    // THIS WILL CLEAR THE TABLE
//    Database.connect("jdbc:sqlite:users.db")
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
    val levelChannel = text("levelChannel")
    val prefix = varchar("prefix", 10)
}
