package com.anonymous.discotimer.viewmodel

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.os.PowerManager
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anonymous.discotimer.R
import com.anonymous.discotimer.data.TimerPreferences
import com.anonymous.discotimer.data.TimerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TimerViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = TimerPreferences(application)
    private val context = application.applicationContext

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private var timerJob: Job? = null
    private var beepPlayer: MediaPlayer? = null
    private var finishPlayer: MediaPlayer? = null
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private var wakeLock: PowerManager.WakeLock? = null

    init {
        loadPreferences()
        initializeMediaPlayers()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            val work = preferences.work.first()
            val cycles = preferences.cycles.first()
            val sets = preferences.sets.first()
            val isMuted = preferences.isMuted.first()

            _timerState.value = _timerState.value.copy(
                work = work,
                cycles = cycles,
                sets = sets,
                isMuted = isMuted
            )
        }
    }

    private fun initializeMediaPlayers() {
        beepPlayer = MediaPlayer.create(context, R.raw.beep)
        finishPlayer = MediaPlayer.create(context, R.raw.finish)
    }

    fun setWork(work: Int) {
        _timerState.value = _timerState.value.copy(work = work)
        viewModelScope.launch {
            preferences.setWork(work)
        }
    }

    fun setCycles(cycles: Int) {
        _timerState.value = _timerState.value.copy(cycles = cycles)
        viewModelScope.launch {
            preferences.setCycles(cycles)
        }
    }

    fun setSets(sets: Int) {
        _timerState.value = _timerState.value.copy(sets = sets)
        viewModelScope.launch {
            preferences.setSets(sets)
        }
    }

    fun toggleMute() {
        val newMutedState = !_timerState.value.isMuted
        _timerState.value = _timerState.value.copy(isMuted = newMutedState)
        viewModelScope.launch {
            preferences.setMuted(newMutedState)
        }
    }

    fun startTimer() {
        acquireWakeLock()
        _timerState.value = _timerState.value.copy(currentTime = 0, isPaused = false, isCompleted = false)

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerState.value.currentTime < _timerState.value.totalTime) {
                if (!_timerState.value.isPaused) {
                    delay(1000)

                    val newTime = _timerState.value.currentTime + 1
                    _timerState.value = _timerState.value.copy(currentTime = newTime)

                    // Play beep sound when work timer is 3 or less
                    if (_timerState.value.currentWorkTime <= 3 && !_timerState.value.isMuted) {
                        playBeep()
                        vibrate()
                    }

                    // Check if timer is complete
                    if (newTime >= _timerState.value.totalTime) {
                        _timerState.value = _timerState.value.copy(isCompleted = true)
                        if (!_timerState.value.isMuted) {
                            playFinish()
                            vibrate()
                        }
                        releaseWakeLock()
                    }
                } else {
                    delay(100) // Check pause state more frequently
                }
            }
        }
    }

    fun togglePause() {
        _timerState.value = _timerState.value.copy(isPaused = !_timerState.value.isPaused)
    }

    fun resetTimer() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(
            currentTime = 0,
            isPaused = false,
            isCompleted = false
        )
        releaseWakeLock()
    }

    private fun playBeep() {
        beepPlayer?.let {
            it.seekTo(0)
            it.start()
        }
    }

    private fun playFinish() {
        finishPlayer?.let {
            it.seekTo(0)
            it.start()
        }
    }

    private fun vibrate() {
        @Suppress("DEPRECATION")
        vibrator.vibrate(200)
    }

    private fun acquireWakeLock() {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "DiscoTimer::TimerWakeLock"
        ).apply {
            acquire(10 * 60 * 60 * 1000L) // 10 hours max
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        beepPlayer?.release()
        finishPlayer?.release()
        releaseWakeLock()
    }
}
