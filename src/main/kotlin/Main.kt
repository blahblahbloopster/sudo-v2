import commands.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.ApplicationInfo
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.io.File

object Main {
    val handler = CommandHandler()
    val jda: JDA
    init {
        handler.commands.add(Help())
        handler.commands.add(Ping())
        handler.commands.add(Xkcd())
        handler.commands.add(Level())
        handler.commands.add(Xp())
        val token = File("token").readText()
        val builder = JDABuilder.createDefault(token)
        builder.setActivity(Activity.watching("for sudo help"))
        jda = builder.build()
        jda.addEventListener(MessageListener())
        jda.awaitReady()
    }
    val applicationInfo: ApplicationInfo = jda.retrieveApplicationInfo().complete()

    class MessageListener : ListenerAdapter() {

        override fun onMessageReceived(event: MessageReceivedEvent) {
            if (event.channel is TextChannel) {
                Points.messageSent(event.author, event.channel as TextChannel)
                handler.process(event.message, ";").send(event.channel as TextChannel)
            }
        }
    }
}

fun main() {
    Main
}
