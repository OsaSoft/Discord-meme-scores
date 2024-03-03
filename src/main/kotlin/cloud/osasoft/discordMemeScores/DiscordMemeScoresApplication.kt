package cloud.osasoft.discordMemeScores

import cloud.osasoft.discordMemeScores.config.DiscordProperties
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import me.jakejmattson.discordkt.dsl.bot

fun main() {
    bot(DiscordProperties.getToken()) {
        configure {
            defaultPermissions = Permissions(
                Permission.ReadMessageHistory,
                Permission.SendMessages,
                Permission.UseExternalEmojis,
            )
        }
    }
}
