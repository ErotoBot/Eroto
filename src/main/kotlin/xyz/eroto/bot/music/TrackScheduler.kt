package xyz.eroto.bot.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import java.util.concurrent.LinkedBlockingQueue

class TrackScheduler(private val player: AudioPlayer, private val manager: GuildMusicManager) : AudioEventAdapter() {
    private val queue = LinkedBlockingQueue<AudioTrack>()

    fun enqueue(track: AudioTrack) {
        if (!player.startTrack(track, true))
            queue.offer(track)
    }

    fun next() = player.startTrack(queue.poll(), false)

    fun shuffle() {
        val tracks = queue.shuffled()

        queue.clear()

        queue += tracks
    }
}