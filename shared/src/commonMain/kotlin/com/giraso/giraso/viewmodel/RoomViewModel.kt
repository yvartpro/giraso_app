package com.giraso.giraso.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RoomViewModel : ViewModel() {
  private val rooms = MutableStateFlow<List<String>>(emptyList())

  fun roomsFlow(): StateFlow<List<String>> = rooms

  fun addRoom(name: String) {
    if (name.isBlank()) return
    if (!rooms.value.contains(name)) {
      rooms.value += name
    }
  }
}
