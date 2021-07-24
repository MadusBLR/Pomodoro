package com.mycompany.pomodoro

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class ForegroundService : Service() {

    private var isServiceStarted = false
    private var notificationManager: NotificationManager? = null
    private var isFinished = false
    private var timeMs=0L
    private var job: Job? = null
    private var startTime = 0L
    private val builder by lazy {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Simple Timer")
            .setGroup("Timer")
            .setGroupSummary(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getPendingIntent())
            .setSilent(true)
            .setSmallIcon(R.drawable.ic_baseline_av_timer_24)
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        processCommand(intent)
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun processCommand(intent: Intent?) {
        when (intent?.extras?.getString(COMMAND_ID)) {
            COMMAND_START -> {
                timeMs = intent.extras?.getLong(STARTED_TIMER_MS)!!
                startTime = intent.extras?.getLong("START_SYSTEM_MS")!!
                commandStart()
            }
            COMMAND_STOP -> commandStop()
            null -> return
        }
    }

    private fun commandStart() {
        if (isServiceStarted) {
            return
        }
        Log.i("TAG", "commandStart()")
        try {
            moveToStartedState()
            startForegroundAndShowNotification()
            continueTimer()

        } finally {
            isServiceStarted = true
        }
    }

    private  fun continueTimer() {

        job = GlobalScope.launch(Dispatchers.Main) {
            while (!isFinished) {
                timeMs -= (SystemClock.elapsedRealtime() - startTime)
                startTime=SystemClock.elapsedRealtime()
                notificationManager?.notify(
                    NOTIFICATION_ID,
                    getNotification(timeMs.getTime())
                )
                delay(INTERVAL)
                if (timeMs < 0L)
                    commandStop()
            }
        }
    }

    private fun commandStop() {
        if (!isServiceStarted) {
            return
        }
        job?.cancel()
        isFinished = true
        Log.i("TAG", "commandStop()")
        try {
            stopForeground(true)
            stopSelf()
        } finally {
            isServiceStarted = false
        }
    }

    private fun moveToStartedState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, ForegroundService::class.java))
        } else {
            startService(Intent(this, ForegroundService::class.java))
        }
    }

    private fun startForegroundAndShowNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "pomodoro"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(
                CHANNEL_ID, channelName, importance
            )
            notificationManager?.createNotificationChannel(notificationChannel)
        }
        val notification = getNotification("content")
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun getNotification(content: String) = builder.setContentText(content).build()



    private fun getPendingIntent(): PendingIntent? {
        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT)
    }

    private companion object {
        private const val CHANNEL_ID = "Channel_ID"
        private const val NOTIFICATION_ID = 777
        private const val INTERVAL = 1000L
    }
}