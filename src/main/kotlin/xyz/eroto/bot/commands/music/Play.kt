package xyz.eroto.bot.commands.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.core.audio.hooks.ConnectionListener
import net.dv8tion.jda.core.audio.hooks.ConnectionStatus
import net.dv8tion.jda.core.entities.User
import okhttp3.HttpUrl
import org.json.JSONObject
import xyz.eroto.bot.Eroto
import xyz.eroto.bot.entities.cmd.Command
import xyz.eroto.bot.entities.cmd.Context
import xyz.eroto.bot.entities.cmd.argument
import xyz.eroto.bot.music.GuildMusicManager
import xyz.eroto.bot.music.MusicManager
import xyz.eroto.bot.utils.Http
import xyz.eroto.bot.utils.ItemPicker
import java.awt.Color

class Play : Command() {
    override val description = "Play music!"
    override val example = "play never gonna give you up"

    init {
        guildOnly = true
        arguments += argument<String>("url", "song")
    }

    override fun run(ctx: Context) {
        if (!ctx.member!!.voiceState.inVoiceChannel()) {
            return ctx.send("You aren't in a voice channel!")
        }

        if (ctx.guild!!.idLong in MusicManager.musicManagers) {
            play(ctx, MusicManager.musicManagers[ctx.guild.idLong])
        } else {
            val manager = MusicManager.join(ctx)

            ctx.guild.audioManager.connectionListener = object : ConnectionListener {
                override fun onStatusChange(status: ConnectionStatus) {
                    if (status == ConnectionStatus.CONNECTED) {
                        play(ctx, manager)
                    }
                }
                override fun onPing(ping: Long) {}
                override fun onUserSpeaking(user: User, speaking: Boolean) {}
            }
        }
    }

    fun play(ctx: Context, manager: GuildMusicManager) {
        val search = ctx.args["url"] as String

        MusicManager.playerManager.loadItemOrdered(manager, search, object : AudioLoadResultHandler {
            override fun loadFailed(exception: FriendlyException) = ctx.sendException(exception)
            override fun noMatches() {
                val picker = ItemPicker(ctx.member!!, true)

                Http.get(HttpUrl.Builder().apply {
                    scheme("https")
                    host("www.googleapis.com")
                    addPathSegments("youtube/v3/search")
                    addQueryParameter("key", Eroto.config.api.google)
                    addQueryParameter("part", "snippet")
                    addQueryParameter("maxResults", "10")
                    addQueryParameter("type", "video")
                    addQueryParameter("q", search)
                }.build()).thenAccept { res ->
                    val items = JSONObject(res.body()!!.string()).getJSONArray("items")

                    for (i in 0 until items.length()) {
                        val item = items.getJSONObject(i)

                        val id = item
                                .getJSONObject("id")
                                .getString("videoId")

                        val snippet = item.getJSONObject("snippet")

                        val title = snippet.getString("title")
                        val thumb = snippet
                                .getJSONObject("thumbnails")
                                .getJSONObject("medium")
                                .getString("url")

                        val channel = snippet.getString("channelTitle")

                        picker.items += ItemPicker.PickerItem(
                                id,
                                title,
                                author = channel,
                                image = thumb,
                                url = "https://youtu.be/$id"
                        )
                    }

                    picker.color = Color(255, 0, 0)

                    picker.build(ctx.channel).thenAccept { item ->
                        MusicManager.playerManager.loadItemOrdered(manager, item.url, object : AudioLoadResultHandler {
                            override fun loadFailed(exception: FriendlyException) = ctx.sendException(exception)

                            override fun noMatches() = ctx.send("No matches :<")

                            override fun trackLoaded(track: AudioTrack) {
                                manager.scheduler.enqueue(track)

                                ctx.send("Added ${track.info.title} from ${track.info.author} to the queue!")
                            }

                            override fun playlistLoaded(playlist: AudioPlaylist) = trackLoaded(playlist.tracks.first())
                        })
                    }

                    res.close()
                }
            }

            override fun trackLoaded(track: AudioTrack) {
                manager.scheduler.enqueue(track)

                ctx.send("Added ${track.info.title} from ${track.info.author} to the queue!")
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                val id = when {
                    search.indexOf("youtu") > -1 -> search.split("v=")[1].split("&")[0]

                    else -> ""
                }

                val tracks = if (id.isNotBlank()) {
                    val index = playlist.tracks.indexOfFirst { it.identifier == id }

                    playlist.tracks.subList(index, playlist.tracks.size)
                } else {
                    playlist.tracks
                }

                for (track in tracks) {
                    manager.scheduler.enqueue(track)
                }

                ctx.send("Added ${tracks.size} tracks from playlist ${playlist.name} to the queue!")
            }
        })
    }
}