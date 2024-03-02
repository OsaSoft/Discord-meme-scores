package cloud.osasoft.discordMemeScores.commands

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.toKotlinInstant
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.util.author
import me.jakejmattson.discordkt.util.isImagePost
import java.time.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

fun memeListener() = commands("Memes") {
    slash("memeStats") {
        execute {
            val cutOffDuration = 7.days
            val cutOffDate = Instant.now() - cutOffDuration.toJavaDuration()

            val author = this.author
            val memes = this.channel.messages
                .takeWhile { it.timestamp >= cutOffDate.toKotlinInstant() }
                .filter { it.author?.id == author.id }
                .filter { it.isImagePost() }
                .toList()

            val reactions: Map<String, Int> = buildMap {
                memes.flatMap { it.reactions }.forEach { reaction ->
                    val name = reaction.emoji.name
                    val currentCount = getOrDefault(name, 0)
                    put(name, currentCount + reaction.count)
                }
            }

            this.respondPublic {
                this.author(author)
                title = "Meme Count"
                description = buildString {
                    append("${author.username} has posted ${memes.size} memes in the past $cutOffDuration")
                    if (memes.isNotEmpty()) {
                        appendLine()
                        append("Gathering the following reactions:")
                        appendLine()
                        reactions.forEach { (emoji, count) -> appendLine("$emoji: $count") }
                    }
                }
            }
        }
    }
}