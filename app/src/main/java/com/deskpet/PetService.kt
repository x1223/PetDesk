package com.deskpet

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout

class PetService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var petView: PetView
    private lateinit var container: FrameLayout
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var petEngine: PetEngine
    private var petServer: PetServer? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())

        petEngine = PetEngine()
        petEngine.onStateChange = { state ->
            petView.setState(state.name.lowercase())
        }

        try {
            petServer = PetServer(petEngine, 8765)
            petServer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setupFloatingWindow()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "pet_desk_channel",
                "PetDesk",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, "pet_desk_channel")
            .setContentTitle("PetDesk Running")
            .setContentText("Tap to interact")
            .setSmallIcon(R.drawable.s1_f01)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun setupFloatingWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 800
        }

        container = FrameLayout(this)
        petView = PetView(this)
        petView.positionListener = object : PetView.OnPositionChangedListener {
            override fun onPositionChanged(deltaX: Int, deltaY: Int) {
                params.x += deltaX
                params.y += deltaY
                windowManager.updateViewLayout(container, params)
            }
        }
        container.addView(petView)

        windowManager.addView(container, params)

        petView.setOnClickListener {
            petView.animateTap()
            petEngine.transition("attention")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        petServer?.stop()
        if (::container.isInitialized) {
            windowManager.removeView(container)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}