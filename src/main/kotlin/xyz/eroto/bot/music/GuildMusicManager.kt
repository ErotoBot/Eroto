package xyz.eroto.bot.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import gnu.trove.list.array.TLongArrayList
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.VoiceChannel

class GuildMusicManager(manager: AudioPlayerManager, val textChannel: TextChannel, val voiceChannel: VoiceChannel) {
    val player: AudioPlayer = manager.createPlayer()
    val scheduler = TrackScheduler(player, this)
    val sendingHandler = AudioPlayerSendHandler(player)
    val voteSkip = TLongArrayList()
    var autoplay = false

    init {
        player.addListener(scheduler)
        player.volume = 50
    }
}