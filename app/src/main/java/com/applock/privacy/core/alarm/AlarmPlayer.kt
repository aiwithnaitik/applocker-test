package com.applock.privacy.core.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object AlarmPlayer {
    private const val TAG = "AlarmPlayer"
    private var mediaPlayer: MediaPlayer? = null
    private var autoStopJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isAlarmActive = MutableStateFlow(false)
    val isAlarmActive: StateFlow<Boolean> = _isAlarmActive.asStateFlow()

    @Synchronized
    fun play(context: Context, durationSeconds: Int = 30) {
        if (_isAlarmActive.value && mediaPlayer?.isPlaying == true) {
            Log.d(TAG, "Alarm already sounding")
            return
        }

        try {
            stop() // Clean up any lingering player

            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(context.applicationContext, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_ALARM)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }

            _isAlarmActive.value = true
            Log.i(TAG, "Intruder alarm activated (duration: ${durationSeconds}s)")

            // Schedule auto-stop to prevent endless sound if unattended
            autoStopJob?.cancel()
            autoStopJob = scope.launch {
                delay(durationSeconds.toLong() * 1000L)
                Log.i(TAG, "Alarm auto-stopped after duration timeout")
                stop()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play intruder alarm", e)
            stop()
        }
    }

    @Synchronized
    fun stop() {
        autoStopJob?.cancel()
        autoStopJob = null
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
                it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping media player", e)
        } finally {
            mediaPlayer = null
            _isAlarmActive.value = false
            Log.i(TAG, "Intruder alarm stopped")
        }
    }

    fun isPlaying(): Boolean = _isAlarmActive.value
}
