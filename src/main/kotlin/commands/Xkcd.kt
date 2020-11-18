package commands

import Command
import EmbedResponse
import ErrorResponse
import NullResponse
import Sendable
import com.beust.klaxon.Klaxon
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import okhttp3.OkHttpClient
import okhttp3.Request
import java.awt.Color
import java.lang.Exception
import java.lang.NumberFormatException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.temporal.TemporalAccessor
import java.util.*

class Xkcd : Command() {
    override val name = "xkcd"

    private val client = OkHttpClient.Builder().followRedirects(false).followSslRedirects(false).build()

    override fun process(args: List<String>, message: Message): Sendable {
        val embed = EmbedBuilder()

        val comicUrl: String

        when (args.size) {
            1 -> {
                val connection = client.newCall(Request.Builder().url("https://c.xkcd.com/random/comic/").build()).execute()
                comicUrl = connection.header("location") ?: return NullResponse()
                connection.close()
            }
            2 -> {
                val num: Int
                try {
                    num = args[1].toInt()
                } catch (e: NumberFormatException) {
                    return ErrorResponse("Not a valid number!")
                }
                if (num < 1) {
                    return ErrorResponse("Not a valid number!")
                }
                comicUrl = "http://xkcd.com/${num}/"
            }
            else -> {
                return NullResponse()
            }
        }
        val jsonUrl = (comicUrl + "info.0.json").replace("http", "https")
        val url = URL(jsonUrl)

        var json: String
        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "GET"  // optional default is GET
            if (responseCode !in 200..299) {
                return NullResponse()
            }
            inputStream.bufferedReader().use {
                json = it.readText()
            }
        }
        data class Comic(val img: String, val title: String, val year: String, val month: String, val day: String, val num: Int, val alt: String)

        val comic = Klaxon().parse<Comic>(json) ?: return NullResponse()

        embed.setTitle("${comic.num}: ${comic.title}")
        val calendar = Calendar.getInstance(SimpleTimeZone.getTimeZone("EST"))
        calendar.set(comic.year.toInt(), comic.month.toInt(), comic.day.toInt())
        embed.setTimestamp(calendar.toInstant())
        embed.setImage(comic.img)
        embed.setFooter(comic.alt)
        embed.setColor(Color.getHSBColor(Random(comic.num.toLong()).nextFloat(), 0.8f, 0.8f))
        return EmbedResponse(embed)
    }
}
