import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import java.io.File

fun main() {
    val token = File("token").readText()
    val builder = JDABuilder.createDefault(token)
    builder.setActivity(Activity.watching("for sudo help"))
    val jda = builder.build()
}
