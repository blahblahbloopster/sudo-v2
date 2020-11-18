package commands

import Command
import Sendable
import TextResponse

class Help : Command() {
    private val name = "help"

    override fun process(args: List<String>): Sendable {
        return TextResponse("Hi!")
    }
}
