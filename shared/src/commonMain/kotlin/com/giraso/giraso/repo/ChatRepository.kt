package com.giraso.giraso.repo

import com.giraso.giraso.domain.ChatMessage
import com.giraso.giraso.domain.Peer
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun sendMessage(room: String, text: String)
    fun joinRoom(room: String)
    fun leaveRoom(room: String)
    fun setOnMessageListener(listener: (ChatMessage) -> Unit)
    
    // New methods from the prompt requirements
    fun observeMessages(): Flow<ChatMessage>
    fun observePeers(): Flow<List<Peer>>
    fun whoami(): String
}
