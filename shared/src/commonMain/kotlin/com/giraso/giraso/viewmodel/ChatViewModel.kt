package com.giraso.giraso.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giraso.giraso.domain.ChatMessage
import com.giraso.giraso.repo.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(private val repo: ChatRepository) : ViewModel() {
  private val messagesPerRoom = mutableMapOf<String, MutableStateFlow<List<ChatMessage>>>()

  init {
    repo.setOnMessageListener { msg ->
      val flow = messagesPerRoom.getOrPut(msg.room) { MutableStateFlow(emptyList()) }
      viewModelScope.launch {
        flow.value += msg
      }
    }
  }

  fun messages(room: String): StateFlow<List<ChatMessage>> {
    return messagesPerRoom.getOrPut(room) { MutableStateFlow(emptyList()) }
  }

  fun send(room: String, text: String) {
    repo.sendMessage(room, text)
  }

  fun whoami(): String = repo.whoami()
}
