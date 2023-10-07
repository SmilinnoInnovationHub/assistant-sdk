package com.smilinno.smilinnolibrary.util

import android.content.Context
import android.media.session.PlaybackState
import android.net.Uri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.smilinno.smilinnolibrary.SmilinnoLibrary

internal object PlayerUtil {

    private var exoPlayer: ExoPlayer? = null
    var isPlaying = false
    private var playerListener: PlayerListener? = null

    fun playVoice(context: Context, url: String?, playerListener: PlayerListener) {
        if (url != null) {
            PlayerUtil.playerListener = playerListener
            isPlaying = true
            initialExoPlayer(context, Uri.parse(url))
        } else {
            // TODO
        }
    }

    private fun initialExoPlayer(context: Context, uri: Uri) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
            exoPlayer?.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    exoPlayer?.prepare()
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    if (playbackState == PlaybackState.STATE_FAST_FORWARDING) {
                        isPlaying = false
                        playerListener?.onStopped()
                    }
                }
            })
            exoPlayer?.playWhenReady = true
        }
        getAudioMediaSource(context,uri).let {
            it.let { it1 -> exoPlayer?.setMediaSource(it1) }
        }
        exoPlayer?.prepare()
        exoPlayer?.play()
    }

    private fun getAudioMediaSource(context: Context, uri: Uri): MediaSource {
        val dataFactory: DataSource.Factory =
            DefaultDataSource.Factory(context)
        val extractorsFactory = DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true)
        return ProgressiveMediaSource.Factory(dataFactory, extractorsFactory)
            .createMediaSource(MediaItem.fromUri(uri))
    }

    fun releasePlayer() {
        isPlaying = false
        if (exoPlayer != null) {
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    interface PlayerListener {
        fun onStopped()
    }

    fun playTTS(context: Context, smilinnoLibrary: SmilinnoLibrary, voice: String, playerListener: PlayerListener) {
        if (smilinnoLibrary.ttsEnabled) {
            if (isPlaying) {
                releasePlayer()
            } else {
                playVoice(context, voice, playerListener)
            }
        }
    }

}