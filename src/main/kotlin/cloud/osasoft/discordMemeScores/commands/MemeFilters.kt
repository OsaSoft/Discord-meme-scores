package cloud.osasoft.discordMemeScores.commands

import dev.kord.core.entity.Message

fun isEmbedMeme(message: Message): Boolean {
    val embedType = message.data.embeds.firstOrNull()?.type?.value.toString()
    return embedType.endsWith(".Gifv") || embedType.endsWith(".Image") || embedType.endsWith(".Video")
}

fun isVideoMeme(message: Message): Boolean {
    val messageAttachment = message.data.attachments.firstOrNull()?.contentType?.value.toString()
    return messageAttachment.startsWith("video")
}