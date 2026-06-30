package com.giraso.giraso.data.network.libp2p

import android.content.Context
import com.giraso.giraso.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "Libp2pDebug"

fun simulateTwoNodes(context: Context) {
    val scope = CoroutineScope(Dispatchers.IO)
    
    scope.launch {
        Logger.i(TAG, "Starting Node A...")
        val clientA = Libp2pClient(context, "nodeA.key")
        clientA.start()
        Logger.i(TAG, "Node A started: ${clientA.whoami()}")
        
        Logger.i(TAG, "Starting Node B...")
        val clientB = Libp2pClient(context, "nodeB.key")
        clientB.start()
        Logger.i(TAG, "Node B started: ${clientB.whoami()}")
        
        delay(3000)
        
        val addressesA = clientA.getListenAddresses()
        Logger.i(TAG, "Node A listen addresses: $addressesA")
        
        val addrA = addressesA.find { it.contains("127.0.0.1") }
            ?: addressesA.find { it.contains("/ip4/") && !it.contains("0.0.0.0") }
            ?: addressesA.firstOrNull()
            
        if (addrA != null) {
            Logger.i(TAG, "Node B dialing Node A at $addrA")
            clientB.dialAddress(addrA)
        } else {
            Logger.e(TAG, "Node A has no listen addresses!")
        }
        
        delay(10000) // Give more time for connection
        
        Logger.i(TAG, "Node A peers: ${clientA.getStats()}") // This is not peers, but stats. 
        // I don't have a getPeers() but I can check stats.
        
        val room = "test-room"
        Logger.i(TAG, "Both nodes joining room: $room")
        clientA.joinRoom(room)
        clientB.joinRoom(room)
        
        delay(10000) // Wait for gossip mesh to form
        
        Logger.i(TAG, "Node A sending message...")
        clientA.sendMessage(room, "Hello from Node A!")
        
        delay(5000)
        
        Logger.i(TAG, "Node B sending message...")
        clientB.sendMessage(room, "Hello from Node B!")
        
        delay(15000)
        Logger.i(TAG, "Simulation finished. Node A stats: ${clientA.getStats()}, Node B stats: ${clientB.getStats()}")
        
        clientA.stop()
        clientB.stop()
    }
}
