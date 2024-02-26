package cloud.osasoft.discordMemeScores.commands

import me.jakejmattson.discordkt.commands.commands

fun testCmds() = commands("Test") {
    slash("ping") {
        execute {
            respond("Pong!")
        }
    }
}