package commands

import Command
import EmbedResponse
import ErrorResponse
import NullResponse
import Sendable
import com.beust.klaxon.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

class Reddit : Command() {
    override val name = "reddit"
    override val help = "Gets a post from a given subreddit"

    private fun data(num: Int, output: String): JsonObject = ((((parse(output) as JsonObject)["data"] as JsonObject)["children"] as JsonArray<*>)[num] as JsonObject)["data"] as JsonObject

    override fun process(args: List<String>, message: Message): Sendable {
        if (args.size < 2) return ErrorResponse("Usage: `reddit r/<subreddit>`")
        if (!args[1].startsWith("r/")) return ErrorResponse("Usage: `reddit r/<subreddit>`")

        val subreddit = safeify(args[1].removePrefix("r/")).slice(0..minOf(args[1].length - 3, 21))
        val output = get("https://api.reddit.com/r/$subreddit/hot")

        val allowedEndings = arrayOf(".png", ".jpg", ".jpeg", ".mp4", ".gif", ".webp", ".webm")

        val json = data(Random.nextInt(0, 30), output)
        if (json["over_18"] as Boolean) return ErrorResponse("NSFW subreddits are not allowed")
        var hasImage = false
        val url = json["url"] as String
        for (ending in allowedEndings) {
            if (ending == url.takeLast(ending.length)) {
                hasImage = true
                break
            }
        }
        val embed = EmbedBuilder()
        if (hasImage) {
            embed.setImage(url)
        }

        embed.setTitle(json["title"] as String?, "https://reddit.com${json["permalink"]}")
        embed.setAuthor(json["author"] as String?)
        embed.addField("Votes", ((json["ups"] as Int) - (json["downs"] as Int)).toString(), true)
        embed.addField("Comments", (json["num_comments"] as Int).toString(), true)
        return EmbedResponse(embed)
    }

    private fun safeify(inp: String): String {
        val sb = StringBuilder()
        for (letter in inp) {
            if (letter in "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_") {
                sb.append(letter)
            }
        }
        return sb.toString()
    }
}

fun get(url: String): String {
    with(URL(url).openConnection() as HttpURLConnection) {
        setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:83.0) Gecko/20100101 Firefox/83.0")
        if (responseCode !in 200..299) {
            println(responseCode)
            return ""
        }
        inputStream.bufferedReader().use {
            return it.readText()
        }
    }
}

fun main() {
    val output = get("https://api.reddit.com/r/memes/hot")
//    data class Item(val type: String)
//    data class Data(val children: JsonArray<Item>)
//    println(output.removePrefix("{\"kind\": \"Listing\", \"data\": ").removeSuffix("}"))
//    println(Klaxon().parse<Data>(output.removePrefix("{\"kind\": \"Listing\", \"data\": ").removeSuffix("}")))
    fun data(num: Int): JsonObject = ((((parse(output) as JsonObject)["data"] as JsonObject)["children"] as JsonArray<*>)[num] as JsonObject)["data"] as JsonObject

    val data = data(2)
    println(data["author"])
    println(data["ups"])
    println(data["url"])
    println(data["title"])
}

fun parse(name: String) : Any? {
    return Parser.default().parse(name.byteInputStream())
//    return cls.getResourceAsStream(name)?.let { inputStream ->
//        return Parser.default().parse(inputStream)
//    }
}
