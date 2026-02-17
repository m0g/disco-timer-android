package com.anonymous.discotimer.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.anonymous.discotimer.MainActivity
import com.anonymous.discotimer.R
import com.anonymous.discotimer.data.TimerState
import com.anonymous.discotimer.utils.TimeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerService : Service() {

    companion object {
        const val ACTION_START = "com.anonymous.discotimer.ACTION_START"
        const val ACTION_PAUSE = "com.anonymous.discotimer.ACTION_PAUSE"
        const val ACTION_TOGGLE_MUTE = "com.anonymous.discotimer.ACTION_TOGGLE_MUTE"
        const val ACTION_RESET = "com.anonymous.discotimer.ACTION_RESET"

        const val EXTRA_WORK = "extra_work"
        const val EXTRA_CYCLES = "extra_cycles"
        const val EXTRA_SETS = "extra_sets"
        const val EXTRA_MUTED = "extra_muted"
        const val EXTRA_PREPARE = "extra_prepare"

        private const val CHANNEL_ID = "disco_timer_channel"
        private const val NOTIFICATION_ID = 1

        private val _timerState = MutableStateFlow(TimerState())
        val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

        var isRunning: Boolean = false
            private set
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var timerJob: Job? = null

    private var whistlePlayer: MediaPlayer? = null
    private var beepPlayer: MediaPlayer? = null
    private var finishPlayer: MediaPlayer? = null
    private lateinit var vibrator: Vibrator
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        createNotificationChannel()
        initializeMediaPlayers()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val work = intent.getIntExtra(EXTRA_WORK, 40)
                val cycles = intent.getIntExtra(EXTRA_CYCLES, 3)
                val sets = intent.getIntExtra(EXTRA_SETS, 2)
                val muted = intent.getBooleanExtra(EXTRA_MUTED, false)
                val prepare = intent.getIntExtra(EXTRA_PREPARE, 0)
                startTimer(work, cycles, sets, muted, prepare)
            }

            ACTION_PAUSE -> togglePause()
            ACTION_TOGGLE_MUTE -> toggleMute()
            ACTION_RESET -> resetAndStop()
        }
        return START_NOT_STICKY
    }

    private fun startTimer(work: Int, cycles: Int, sets: Int, muted: Boolean, prepare: Int = 0) {
        isRunning = true
        acquireWakeLock()

        val hasPrepare = prepare > 0

        _timerState.value = TimerState(
            work = work,
            cycles = cycles,
            sets = sets,
            prepare = prepare,
            currentTime = 0,
            isPaused = false,
            isCompleted = false,
            isMuted = muted,
            isPreparing = hasPrepare,
            prepareTimeRemaining = prepare
        )

        startForeground(NOTIFICATION_ID, buildNotification())

        timerJob?.cancel()
        timerJob = serviceScope.launch {
            // Prepare countdown phase
            if (hasPrepare) {
                while (_timerState.value.prepareTimeRemaining > 0) {
                    if (!_timerState.value.isPaused) {
                        var elapsedMs = 0
                        while (elapsedMs < 1000 && !_timerState.value.isPaused) {
                            delay(50)
                            elapsedMs += 50
                        }

                        if (!_timerState.value.isPaused) {
                            val newRemaining = _timerState.value.prepareTimeRemaining - 1
                            _timerState.value = _timerState.value.copy(prepareTimeRemaining = newRemaining)

                            if (newRemaining <= 3 && newRemaining > 0 && !_timerState.value.isMuted) {
                                playBeep()
                                vibrate()
                            }

                            updateNotification()
                        }
                    } else {
                        delay(50)
                    }
                }

                // Transition from prepare to workout
                _timerState.value = _timerState.value.copy(isPreparing = false)
            }

            // Play whistle at workout start
            if (!_timerState.value.isMuted) {
                playWhistle()
                vibrate()
            }

            // Main workout timer loop
            while (_timerState.value.currentTime < _timerState.value.totalTime) {
                if (!_timerState.value.isPaused) {
                    var elapsedMs = 0
                    while (elapsedMs < 1000 && !_timerState.value.isPaused) {
                        delay(50)
                        elapsedMs += 50
                    }

                    if (!_timerState.value.isPaused) {
                        val newTime = _timerState.value.currentTime + 1
                        _timerState.value = _timerState.value.copy(currentTime = newTime)

                        if (_timerState.value.currentWorkTime <= 3 && !_timerState.value.isMuted) {
                            playBeep()
                            vibrate()
                        }

                        if (_timerState.value.currentWorkTime == _timerState.value.work && !_timerState.value.isMuted) {
                            playWhistle()
                            vibrate()
                        }

                        if (newTime >= _timerState.value.totalTime) {
                            _timerState.value = _timerState.value.copy(isCompleted = true)
                            if (!_timerState.value.isMuted) {
                                playFinish()
                                vibrate()
                            }
                            releaseWakeLock()
                            stopForeground(STOP_FOREGROUND_REMOVE)
                            isRunning = false
                            stopSelf()
                            return@launch
                        }

                        updateNotification()
                    }
                } else {
                    delay(50)
                }
            }
        }
    }

    private fun togglePause() {
        _timerState.value = _timerState.value.copy(isPaused = !_timerState.value.isPaused)
        updateNotification()
    }

    private fun toggleMute() {
        _timerState.value = _timerState.value.copy(isMuted = !_timerState.value.isMuted)
    }

    private fun resetAndStop() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(
            currentTime = 0,
            isPaused = false,
            isCompleted = false,
            isPreparing = false,
            prepareTimeRemaining = 0
        )
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        isRunning = false
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Disco Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows timer countdown progress"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): android.app.Notification {
        val state = _timerState.value
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentTitle: String
        val contentText: String

        if (state.isPreparing) {
            contentTitle = TimeFormatter.formatSeconds(state.prepareTimeRemaining)
            contentText = if (state.isPaused) "Paused" else "Get ready..."
        } else {
            contentTitle = TimeFormatter.formatSeconds(state.currentWorkTime)
            val timeText = TimeFormatter.formatSeconds(state.remainingTime)
            contentText =
                if (state.isPaused) "Paused" else "Total ${timeText} - Set ${state.currentSet} - Cycle ${state.currentCycle}"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_timer)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(pendingIntent)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun updateNotification() {
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun initializeMediaPlayers() {
        whistlePlayer = MediaPlayer.create(this, R.raw.whistle)
        beepPlayer = MediaPlayer.create(this, R.raw.beep)
        finishPlayer = MediaPlayer.create(this, R.raw.finish)
    }

    private fun playWhistle() {
        whistlePlayer?.let { it.seekTo(0); it.start() }
    }

    private fun playBeep() {
        beepPlayer?.let { it.seekTo(0); it.start() }
    }

    private fun playFinish() {
        finishPlayer?.let { it.seekTo(0); it.start() }
    }

    private fun vibrate() {
        @Suppress("DEPRECATION")
        vibrator.vibrate(200)
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "DiscoTimer::ServiceWakeLock"
        ).apply {
            acquire(10 * 60 * 60 * 1000L)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        resetAndStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        serviceScope.cancel()
        whistlePlayer?.release()
        beepPlayer?.release()
        finishPlayer?.release()
        releaseWakeLock()
        isRunning = false
    }
}
