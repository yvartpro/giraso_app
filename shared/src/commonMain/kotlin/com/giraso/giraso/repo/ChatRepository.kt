package com.giraso.giraso.repo

import com.giraso.giraso.domain.ChatMessage
import com.giraso.giraso.transport.ChatTransport

class ChatRepository(private val transport: ChatTransport) {
  fun sendMessage(room: String, text: String) {
    transport.send(room, text)
  }

  fun joinRoom(room: String) {
    transport.join(room)
  }

  fun leaveRoom(room: String) {
    transport.leave(room)
  }

  fun setOnMessageListener(listener: (ChatMessage) -> Unit) {
    transport.setOnMessageListener(listener)
  }
}
