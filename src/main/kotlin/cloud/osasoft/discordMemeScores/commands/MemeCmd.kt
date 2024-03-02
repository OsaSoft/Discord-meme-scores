package cloud.osasoft.discordMemeScores.commands

import dev.kord.core.entity.Message
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.toKotlinInstant
import me.jakejmattson.discordkt.TypeContainer
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.util.author
import me.jakejmattson.discordkt.util.isImagePost
import me.jakejmattson.discordkt.util.toPartialEmoji
import java.time.Instant
import java.util.regex.Pattern
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

fun memeListener() = commands("Memes") {

    val emojiPattern = Pattern.compile("\\p{IsEmoji}").toRegex()

    suspend fun <T : TypeContainer> getMemes(
        event: GuildSlashCommandEvent<T>,
        cutOffDate: Instant,
    ): List<Message> = event.channel.messages
        .takeWhile { it.timestamp >= cutOffDate.toKotlinInstant() }
        .filter { it.author?.id == event.author.id }
        .filter { it.isImagePost() }
        .toList()

    slash("memeStats") {
        execute {
            val cutOffDuration = 7.days
            val cutOffDate = Instant.now() - cutOffDuration.toJavaDuration()

            val author = this.author
            val memes = getMemes(this, cutOffDate)

            val reactions: Map<String, Int> = buildMap {
                memes.flatMap { it.reactions }.forEach { reaction ->
                    val name = if (reaction.emoji.name.matches(emojiPattern)) {
                        reaction.emoji.name
                    } else { // Custom emoji
                        val name = reaction.emoji.name
                        val id = reaction.emoji.toPartialEmoji().id?.value ?: ""
                        "<:$name:$id>"
                    }
                    val currentCount = getOrDefault(name, 0)
                    put(name, currentCount + reaction.count)
                }
            }

            this.respondPublic {
                this.author(author)
                title = "Meme Stats"
                description = buildString {
                    append("${author.username} has posted ${memes.size} memes in the past $cutOffDuration")
                    if (memes.isNotEmpty()) {
                        appendLine()
                        append("Gathering the following reactions:")
                        appendLine()
                        reactions.forEach { (emoji, count) -> appendLine("$emoji : $count") }
                    }
                }
            }
        }
    }

    slash("memeScore") {
        execute {
            val cutOffDuration = 7.days
            val cutOffDate = Instant.now() - cutOffDuration.toJavaDuration()

            val author = this.author
            val memes = getMemes(this, cutOffDate)

            var positiveScore = 0
            var negativeScore = 0
            memes.flatMap { it.reactions }.forEach { reaction ->
                when (reaction.emoji.name) {
                    "old" -> negativeScore += (5 * reaction.count)
                    "tooold" -> negativeScore += (10 * reaction.count)
                    else -> positiveScore += reaction.count
                }
            }

            val score = positiveScore - negativeScore

            this.respondPublic {
                this.author(author)
                title = "Meme Score: $score"
                description = "Positive score: $positiveScore, Negative score: $negativeScore"
            }
        }
    }
}