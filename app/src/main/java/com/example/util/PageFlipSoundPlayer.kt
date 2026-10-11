package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

/**
 * High-performance, zero-latency physical page flip sound engine.
 * Pre-synthesizes realistic tactile paper rustle samples into memory.
 * Plays instantly on GPU page curl without blocking main thread or causing frame drops.
 */
object PageFlipSoundPlayer {

    private var isInitialized = false
    private var pcmBuffer: ShortArray? = null
    private var audioTrack: AudioTrack? = null

    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val sampleRate = 44100
                val durationMs = 110
                val numSamples = (sampleRate * durationMs / 1000)
                val buffer = ShortArray(numSamples)

                val rnd = Random(42)
                var lastNoise = 0f

                // Synthesize organic tactile paper friction & swish sound
                for (i in 0 until numSamples) {
                    val t = i.toFloat() / numSamples.toFloat()
                    // Envelope: fast rise, gentle parabolic decay
                    val envelope = if (t < 0.15f) {
                        (t / 0.15f)
                    } else {
                        val decay = 1f - (t - 0.15f) / 0.85f
                        decay * decay
                    }

                    // Low-pass filtered pink noise simulating paper fibers brushing
                    val white = rnd.nextFloat() * 2f - 1f
                    lastNoise = 0.82f * lastNoise + 0.18f * white

                    // Soft whisper tone
                    val tone = sin(2.0 * Math.PI * 480.0 * i / sampleRate).toFloat() * 0.15f

                    val sample = (lastNoise * 0.85f + tone) * envelope * 0.45f
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                pcmBuffer = buffer

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(numSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                audioTrack = track
            } catch (e: Exception) {
                android.util.Log.e("PageFlipSound", "Failed to init page flip sound", e)
            }
        }
    }

    fun playSound() {
        try {
            val track = audioTrack ?: return
            track.stop()
            track.reloadStaticData()
            track.play()
        } catch (_: Exception) {}
    }
}
