import commands.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.ApplicationInfo
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
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
        handler.commands.add(Setting())
        val token = File("token").readText()
        val builder = JDABuilder.createDefault(token)
        builder.setActivity(Activity.watching("for sudo help"))
        jda = builder.build()
        jda.addEventListener(MessageListener())
        jda.addEventListener(ReactionListener())
        jda.awaitReady()
    }
    val applicationInfo: ApplicationInfo = jda.retrieveApplicationInfo().complete()

    class MessageListener : ListenerAdapter() {

        override fun onMessageReceived(event: MessageReceivedEvent) {
            if (event.channel is TextChannel) {
                if (event.member != null) DB.messageSent(event.member!!, event.channel as TextChannel)
                handler.process(event.message, DB.getPrefix(event.guild)).send(event.channel as TextChannel)
            }
        }
    }

    class ReactionListener : ListenerAdapter() {

        override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
            if (!DB.getHallOfFameEnabled(event.guild)) return
            val message = event.channel.retrieveMessageById(event.messageId).complete()

            val hofReaction = DB.getHallOfFameEmoji(event.guild) ?: return

            val emotes = event.guild.getEmotesByName(hofReaction, true)
            if (emotes.isEmpty()) return
            val users = message.retrieveReactionUsers(emotes[0]).limit(50).complete()
            if (users.contains(jda.selfUser)) return

            val count = message.reactions.find { it.reactionEmote.name == hofReaction }?.count ?: return
            if (count >= DB.getHallOfFameThreshold(message.guild)) {
                val channel = DB.getHallOfFameChannel(event.guild) ?: return
                val embed = EmbedBuilder()
                embed.setThumbnail(message.author.avatarUrl)
                embed.setTitle("Hall of Fame")
                embed.addField("User", message.author.asMention, true)
                if (message.contentRaw.isNotBlank()) {
                    embed.addField("Message", message.contentRaw, false)
                }
                if (message.attachments.isNotEmpty()) {
                    if (message.attachments[0].isImage) {
                        embed.setImage(message.attachments[0].url)
                    }
                }
                embed.setColor(message.member?.color)
                EmbedResponse(embed).send(channel)
                message.addReaction(emotes[0]).complete()
            }
        }
    }
}

fun main() {
    Main
}
