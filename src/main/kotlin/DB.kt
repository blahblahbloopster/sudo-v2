import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.math.max

object DB {
    init {
        Database.connect("jdbc:sqlite:users.db")
    }

    fun getPoints(id: String): Int {
        var pnts: Int? = null
        transaction {
            for (user in Users.select { Users.id eq id }) {
                pnts = user[Users.points]
                break
            }
        }
        pnts ?: run {
            transaction {
                Users.insert {
                    it[Users.id] = id
                    it[Users.points] = 0
                }
            }
            pnts = 0
        }
        return pnts!!
    }

    fun setPoints(id: String, points: Int) {
        transaction {
            var exists = false
            for (user in Users.select { Users.id eq id }) {
                exists = true
            }
            if (!exists) {
                Users.insert {
                    it[Users.id] = id
                    it[Users.points] = 0
                }
            }
            Users.update({ Users.id eq id}) {
                it[Users.points] = max(points, 0)
            }
        }
    }

    fun printUsers() {
        transaction {
            for (user in Users.selectAll()) {
                println("${user[Users.id]}: ${user[Users.points]}")
            }
        }
    }

    fun incrementPoints(id: String, diff: Int): Int {
        val points = max(getPoints(id) + diff, 0)
        setPoints(id, points)
        return points
    }
}

fun main() {
    // THIS WILL CLEAR THE TABLE
    Database.connect("jdbc:sqlite:users.db")
    transaction {
        SchemaUtils.drop(Users)
        SchemaUtils.create(Users)
    }
}

object Users : Table() {
    val id = varchar("id", 25)
    val points = integer("points")
}
