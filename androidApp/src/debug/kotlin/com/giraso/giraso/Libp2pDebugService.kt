package com.giraso.giraso

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.giraso.giraso.data.network.libp2p.simulateTwoNodes
import com.giraso.giraso.utils.Logger
import kotlinx.coroutines.*

class Libp2pDebugService : Service() {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Libp2pDebugService created")
        
        // Create notification channel for foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Libp2p Debug", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
        
        // Create foreground notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Libp2p E2E Test")
            .setContentText("Running end-to-end test...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        startForeground(NOTIFICATION_ID, notification)
        
        scope.launch {
            try {
                Logger.i(TAG, "Starting simulateTwoNodes...")
                simulateTwoNodes(this@Libp2pDebugService)
                Logger.i(TAG, "simulateTwoNodes completed successfully")
            } catch (e: CancellationException) {
                Logger.i(TAG, "simulateTwoNodes cancelled: ${e.message}")
            } catch (e: Exception) {
                Logger.e(TAG, "simulateTwoNodes error: ${e.message}")
                e.printStackTrace()
            } finally {
                Logger.i(TAG, "Stopping service")
                stopForeground(true)
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        Logger.i(TAG, "Libp2pDebugService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.i(TAG, "onStartCommand received")
        return START_REDELIVER_INTENT
    }

    companion object {
        private const val TAG = "Libp2pDebugService"
        private const val CHANNEL_ID = "libp2p_debug"
        private const val NOTIFICATION_ID = 1001
    }
}
