package xyz.eroto.bot.music

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import gnu.trove.map.hash.TLongObjectHashMap
import net.dv8tion.jda.core.entities.VoiceChannel
import xyz.eroto.bot.entities.cmd.Context

object MusicManager {
    val playerManager = DefaultAudioPlayerManager()
    val musicManagers = TLongObjectHashMap<GuildMusicManager>()

    init {
        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)
    }

    fun join(ctx: Context): GuildMusicManager {
        val manager = GuildMusicManager(playerManager, ctx.event.textChannel, ctx.member!!.voiceState.channel as VoiceChannel)
        musicManagers.put(ctx.guild!!.idLong, manager)
        ctx.guild.audioManager.openAudioConnection(ctx.member.voiceState?.channel)
        ctx.guild.audioManager.sendingHandler = manager.sendingHandler

        return manager
    }

    fun leave(guild: Long): Boolean {
        val manager = musicManagers.remove(guild) ?: return false
        manager.player.stopTrack()
        manager.voiceChannel.guild.audioManager.closeAudioConnection()

        return true
    }
}