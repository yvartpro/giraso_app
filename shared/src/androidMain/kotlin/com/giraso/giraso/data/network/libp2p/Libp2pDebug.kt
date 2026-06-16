package com.giraso.giraso.data.network.libp2p

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

suspend fun simulateTwoNodes(context: Context, logger: (String) -> Unit = { println(it) }) {
    val nodeA = Libp2pClient(context, "nodeA.key")
    val nodeB = Libp2pClient(context, "nodeB.key")

    logger("[simulateTwoNodes] starting nodes")
    nodeA.start()
    nodeB.start()

    logger("[simulateTwoNodes] nodes started, joining room test-room")
    nodeA.joinRoom("test-room")
    nodeB.joinRoom("test-room")

    val job = CoroutineScope(Dispatchers.IO).launch {
        val collectA = launch {
            nodeA.messages.collect { msg ->
                logger("[NodeA] received: $msg")
            }
        }
        val collectB = launch {
            nodeB.messages.collect { msg ->
                logger("[NodeB] received: $msg")
            }
        }
        delay(2000)
        logger("[simulateTwoNodes] NodeA sending message")
        nodeA.sendMessage("test-room", "Hello from A")
        delay(500)
        logger("[simulateTwoNodes] NodeB sending message")
        nodeB.sendMessage("test-room", "Hello from B")
        delay(2000)
        collectA.cancel()
        collectB.cancel()
    }

    job.join()

    logger("[simulateTwoNodes] leaving room and stopping nodes")
    nodeA.leaveRoom("test-room")
    nodeB.leaveRoom("test-room")

    nodeA.stop()
    nodeB.stop()
}
