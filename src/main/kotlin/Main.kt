import java.io.File

fun main() {
    val token = File("token");
    println(token.readText())
}
