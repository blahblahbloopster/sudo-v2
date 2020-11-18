import commands.Help
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.io.File

val handler = CommandHandler()

fun main() {
    handler.commands.add(Help())

    val token = File("token").readText()
    val builder = JDABuilder.createDefault(token)
    builder.setActivity(Activity.watching("for sudo help"))
    val jda = builder.build()
    jda.addEventListener(MessageListener())
    jda.awaitReady()
}

class MessageListener : ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.channel is TextChannel) {
            handler.process(event.message, ";").send(event.channel as TextChannel)
        }
    }
}
