package com.giraso.giraso

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.giraso.giraso.data.network.libp2p.simulateTwoNodes
import kotlinx.coroutines.*

class Libp2pDebugService : Service() {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Libp2pDebugService created")
        scope.launch {
            try {
                simulateTwoNodes(this@Libp2pDebugService) { msg ->
                    Log.d(TAG, msg)
                }
                Log.i(TAG, "simulateTwoNodes completed")
            } catch (e: CancellationException) {
                Log.i(TAG, "simulateTwoNodes cancelled")
            } catch (e: Exception) {
                Log.e(TAG, "simulateTwoNodes error", e)
            } finally {
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        Log.i(TAG, "Libp2pDebugService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand received")
        return START_NOT_STICKY
    }

    companion object {
        private const val TAG = "Libp2pDebugService"
    }
}
