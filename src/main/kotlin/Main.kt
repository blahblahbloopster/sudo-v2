import commands.Help
import commands.Ping
import commands.Xkcd
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
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
        val token = File("token").readText()
        val builder = JDABuilder.createDefault(token)
        builder.setActivity(Activity.watching("for sudo help"))
        jda = builder.build();
        jda.addEventListener(MessageListener())
    }

    class MessageListener : ListenerAdapter() {

        override fun onMessageReceived(event: MessageReceivedEvent) {
            if (event.channel is TextChannel) {
                handler.process(event.message, ";").send(event.channel as TextChannel)
            }
        }
    }
}

fun main() {
    Main
}
