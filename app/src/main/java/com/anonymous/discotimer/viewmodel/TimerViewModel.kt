package com.anonymous.discotimer.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anonymous.discotimer.data.TimerPreferences
import com.anonymous.discotimer.data.TimerState
import com.anonymous.discotimer.service.TimerService
import kotlinx.coroutines.Job
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

    private var observeJob: Job? = null

    init {
        loadPreferences()
        if (TimerService.isRunning) {
            observeServiceState()
        }
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
        if (TimerService.isRunning) {
            sendServiceAction(TimerService.ACTION_TOGGLE_MUTE)
        }
    }

    fun startTimer() {
        val state = _timerState.value
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START
            putExtra(TimerService.EXTRA_WORK, state.work)
            putExtra(TimerService.EXTRA_CYCLES, state.cycles)
            putExtra(TimerService.EXTRA_SETS, state.sets)
            putExtra(TimerService.EXTRA_MUTED, state.isMuted)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        observeServiceState()
    }

    fun togglePause() {
        sendServiceAction(TimerService.ACTION_PAUSE)
    }

    fun resetTimer() {
        sendServiceAction(TimerService.ACTION_RESET)
        observeJob?.cancel()
        observeJob = null
    }

    private fun sendServiceAction(action: String) {
        val intent = Intent(context, TimerService::class.java).apply {
            this.action = action
        }
        context.startService(intent)
    }

    private fun observeServiceState() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            TimerService.timerState.collect { serviceState ->
                _timerState.value = serviceState.copy(isMuted = _timerState.value.isMuted)
            }
        }
    }
}
