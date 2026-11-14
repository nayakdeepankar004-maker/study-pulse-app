package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.model.AlarmTone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sin

class StudySoundPlayer(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(Dispatchers.Default)
    private var alarmLoopJob: Job? = null

    fun startAlarmLoop(tone: AlarmTone) {
        stopAlarm()
        alarmLoopJob = scope.launch {
            while (isActive) {
                playSynthBurst(tone)
                vibratePulse()
                delay(1100L)
            }
        }
    }

    fun previewTone(tone: AlarmTone) {
        scope.launch {
            playSynthBurst(tone)
            vibratePulse()
        }
    }

    fun playCelebrationChime() {
        scope.launch {
            val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
            for (freq in notes) {
                playSingleWave(
                    waveform = Waveform.TRIANGLE,
                    startFreq = freq,
                    endFreq = freq,
                    durationMs = 260,
                    volume = 0.28
                )
                delay(65L)
            }
        }
    }

    fun stopAlarm() {
        alarmLoopJob?.cancel()
        alarmLoopJob = null
    }

    private fun playSynthBurst(tone: AlarmTone) {
        when (tone) {
            AlarmTone.CYBER -> playSingleWave(
                waveform = Waveform.TRIANGLE,
                startFreq = 784.0,
                endFreq = 1046.0,
                durationMs = 420,
                volume = 0.35
            )
            AlarmTone.URGENT -> playSingleWave(
                waveform = Waveform.SAWTOOTH,
                startFreq = 950.0,
                endFreq = 650.0,
                durationMs = 420,
                volume = 0.32
            )
            AlarmTone.NEON -> playSingleWave(
                waveform = Waveform.SINE,
                startFreq = 587.33,
                endFreq = 880.0,
                durationMs = 420,
                volume = 0.38
            )
        }
    }

    private enum class Waveform { SINE, TRIANGLE, SAWTOOTH }

    private fun playSingleWave(
        waveform: Waveform,
        startFreq: Double,
        endFreq: Double,
        durationMs: Int,
        volume: Double
    ) {
        val sampleRate = 22050
        val totalSamples = (sampleRate * durationMs) / 1000
        if (totalSamples <= 0) return

        val pcm = ShortArray(totalSamples)
        var phase = 0.0
        val switchPoint = (totalSamples * 0.42).toInt()

        for (i in 0 until totalSamples) {
            val freq = if (i < switchPoint) startFreq else endFreq
            val t = i.toDouble() / totalSamples.toDouble()
            val env = exp(-4.2 * t)
            phase += freq / sampleRate
            val frac = phase - phase.toLong()
            val raw = when (waveform) {
                Waveform.SINE -> sin(2.0 * PI * frac)
                Waveform.TRIANGLE -> 2.0 * abs(2.0 * frac - 1.0) - 1.0
                Waveform.SAWTOOTH -> 2.0 * frac - 1.0
            }
            val sample = (raw * env * volume * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            pcm[i] = sample.toShort()
        }

        runCatching {
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(pcm.size * 2)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
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
                .setBufferSizeInBytes(minBuf)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcm, 0, pcm.size)
            track.play()
            Thread.sleep(durationMs.toLong() + 30L)
            track.stop()
            track.release()
        }
    }

    @Suppress("DEPRECATION")
    private fun vibratePulse() {
        runCatching {
            val pattern = longArrayOf(0, 220, 110, 220)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                val vibrator = appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    vibrator?.vibrate(pattern, -1)
                }
            }
        }
    }
}
