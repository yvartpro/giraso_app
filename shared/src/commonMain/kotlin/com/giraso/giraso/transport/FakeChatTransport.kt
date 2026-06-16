package com.giraso.giraso.transport

import com.giraso.giraso.domain.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.random.Random

class FakeChatTransport : ChatTransport {
    private val joined = mutableSetOf<String>()
    private var listener: ((ChatMessage) -> Unit)? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun send(room: String, message: String) {
        // simulate network delay and echo to listener
        scope.launch {
            delay(Random.nextLong(100, 300))
            val msg = ChatMessage(
                id = Random.nextLong().toString(),
                room = room,
                from = "me",
                text = message,
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
            listener?.invoke(msg)
        }
    }

    override fun join(room: String) {
        joined.add(room)
    }

    override fun leave(room: String) {
        joined.remove(room)
    }

    override fun setOnMessageListener(listener: (ChatMessage) -> Unit) {
        this.listener = listener
    }
}
