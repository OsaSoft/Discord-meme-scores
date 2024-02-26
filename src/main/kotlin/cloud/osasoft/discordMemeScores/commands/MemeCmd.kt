package cloud.osasoft.discordMemeScores.commands

import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filter
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.util.isImagePost

fun memeListener() = commands("Memes") {
    slash("countMemes") {
        execute {
            val count = this.channel.messages
                .filter { it.author?.id == this.author.id }
                .filter { it.isImagePost() }
                .count()

            this.respondPublic {
                title = "Meme Count"
                description = "${this.author} has posted $count memes."
            }
        }
    }
}