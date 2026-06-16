package com.giraso.giraso.data.network.libp2p

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

suspend fun simulateTwoNodes(context: Context) {
    val nodeA = Libp2pClient(context, "nodeA.key")
    val nodeB = Libp2pClient(context, "nodeB.key")

    nodeA.start()
    nodeB.start()

    nodeA.joinRoom("test-room")
    nodeB.joinRoom("test-room")

    val job = CoroutineScope(Dispatchers.IO).launch {
        val collectA = launch {
            nodeA.messages.collect { msg ->
                println("NodeA received: $msg")
            }
        }
        val collectB = launch {
            nodeB.messages.collect { msg ->
                println("NodeB received: $msg")
            }
        }
        delay(2000)
        nodeA.sendMessage("test-room", "Hello from A")
        delay(500)
        nodeB.sendMessage("test-room", "Hello from B")
        delay(2000)
        collectA.cancel()
        collectB.cancel()
    }

    job.join()

    nodeA.leaveRoom("test-room")
    nodeB.leaveRoom("test-room")

    nodeA.stop()
    nodeB.stop()
}
