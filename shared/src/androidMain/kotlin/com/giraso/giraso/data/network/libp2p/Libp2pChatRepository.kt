package com.giraso.giraso.data.network.libp2p

import com.giraso.giraso.domain.ChatMessage
import com.giraso.giraso.domain.Peer
import com.giraso.giraso.repo.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.random.Random

class Libp2pChatRepository(
    private val client: Libp2pClient
) : ChatRepository {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var messageListener: ((ChatMessage) -> Unit)? = null
    private val peerTracker = Libp2pPeerTracker(client)

    private val _messagesFlow = MutableSharedFlow<ChatMessage>(extraBufferCapacity = 100)

    init {
        client.start()
        scope.launch {
            client.messages.collect { libp2pMsg ->
                val chatMsg = ChatMessage(
                    id = Random.nextLong().toString(),
                    room = libp2pMsg.room,
                    from = libp2pMsg.from,
                    text = libp2pMsg.text,
                    timestamp = libp2pMsg.timestamp
                )
                messageListener?.invoke(chatMsg)
                _messagesFlow.emit(chatMsg)
            }
        }
    }

    override fun sendMessage(room: String, text: String) {
        client.sendMessage(room, text)
    }

    override fun joinRoom(room: String) {
        client.joinRoom(room)
    }

    override fun leaveRoom(room: String) {
        client.leaveRoom(room)
    }

    override fun setOnMessageListener(listener: (ChatMessage) -> Unit) {
        this.messageListener = listener
    }

    override fun observeMessages(): Flow<ChatMessage> = _messagesFlow.asSharedFlow()

    override fun observePeers(): Flow<List<Peer>> = peerTracker.observePeers()

    override fun whoami(): String = client.whoami()
}
