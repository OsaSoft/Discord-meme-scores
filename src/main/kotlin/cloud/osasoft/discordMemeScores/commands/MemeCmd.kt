package cloud.osasoft.discordMemeScores.commands

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.effectiveName
import dev.kord.rest.builder.message.embed
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.toKotlinInstant
import me.jakejmattson.discordkt.TypeContainer
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.util.*
import java.time.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

fun memeListener() = commands("Memes") {


    suspend fun <T : TypeContainer> getMemes(
        event: GuildSlashCommandEvent<T>,
        cutOffDate: Instant,
    ): List<Message> = event.channel.getMessagesAfter(messageId = Snowflake(cutOffDate.toKotlinInstant()))
        .filter { it.author?.id == event.author.id }
        .filter { message ->
            message.isImagePost() || isEmbedMeme(message) || isVideoMeme(message)
        }
        .toList()

    slash("memeStats") {
        execute {
            val cutOffDuration = 7.days
            respond { description = "Gathering meme stats for the past $cutOffDuration..." }

            val cutOffDate = Instant.now() - cutOffDuration.toJavaDuration()

            val author = this.author
            val memes = getMemes(this, cutOffDate)

            val reactions: Map<String, Int> = buildMap {
                memes.flatMap { it.reactions }.forEach { reaction ->
                    val name = if (reaction.emoji is ReactionEmoji.Unicode) {
                        reaction.emoji.name
                    } else { // Custom emoji
                        val customEmoji = reaction.emoji.urlFormat
                        "<:$customEmoji>"
                    }
                    val currentCount = getOrDefault(name, 0)
                    put(name, currentCount + reaction.count)
                }
            }

            channel.createMessage {
                embed {
                    author {
                        this.name = author.effectiveName
                        this.icon = author.pfpUrl
                        this.url = author.profileLink
                    }
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
    }

    slash("memeScore") {
        execute {
            val cutOffDuration = 7.days
            respond { description = "Calculating meme score for the past $cutOffDuration..." }
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

            channel.createMessage {
                embed {
                    author {
                        this.name = author.effectiveName
                        this.icon = author.pfpUrl
                        this.url = author.profileLink
                    }
                    title = "Meme Score: $score"
                    description = "Positive score: $positiveScore, Negative score: $negativeScore"
                }

            }
        }
    }
}