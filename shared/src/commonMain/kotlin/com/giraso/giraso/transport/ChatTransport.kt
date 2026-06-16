package com.giraso.giraso.transport

import com.giraso.giraso.domain.ChatMessage

interface ChatTransport {
    fun send(room: String, message: String)
    fun join(room: String)
    fun leave(room: String)
    fun setOnMessageListener(listener: (ChatMessage) -> Unit)
}
