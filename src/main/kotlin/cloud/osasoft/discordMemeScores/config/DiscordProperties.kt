package cloud.osasoft.discordMemeScores.config

import java.util.Properties

object DiscordProperties {

    private val properties = Properties()

    init {
        val file = this::class.java.classLoader.getResourceAsStream("discord.properties")
        properties.load(file)
    }

    fun getToken(): String = getProperty("token")

    private fun getProperty(key: String): String = properties.getProperty(key)
}