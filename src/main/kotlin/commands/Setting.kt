package commands

import AdminCommand
import EmbedResponse
import ErrorResponse
import NullResponse
import Sendable
import TextResponse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message

class Setting : AdminCommand("setting") {
    override val help = "To list all settings: `setting`  To change a setting: `setting <name> <value>`"

    override fun process(args: List<String>, message: Message): Sendable {
        val output = super.process(args, message)
        if (output !is NullResponse) {
            return output
        }
        if (args.size < 3) {
            val embed = EmbedBuilder()
            for (setting in DB.getAllSettings(message.guild)) {
                embed.addField(setting.first.name, setting.second.toString(), true)
            }
            return EmbedResponse(embed)
        }
        val name = args[1]
        if (!DB.settings.map { it.name }.contains(name)) return ErrorResponse("Not a known setting")
        val value = args.minus(args.take(2)).reduceRight { s, acc -> "$s $acc" }
        if (value.isBlank()) return ErrorResponse("Not enough arguments")

        try {
            DB.settings.find { it.name == name }?.set(value, message.guild)
        } catch (e: RuntimeException) {
            return ErrorResponse(e.message ?: return ErrorResponse("Failed to set \"$name\""))
        }
        return TextResponse("Value set successfully")
    }
}
