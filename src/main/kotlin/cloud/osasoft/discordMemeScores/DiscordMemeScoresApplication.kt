package cloud.osasoft.discordMemeScores

import cloud.osasoft.discordMemeScores.config.DiscordProperties
import me.jakejmattson.discordkt.dsl.bot

fun main() {
    bot(DiscordProperties.getToken()) {}
}
