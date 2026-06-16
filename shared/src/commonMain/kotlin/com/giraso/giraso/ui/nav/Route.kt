package com.giraso.giraso.ui.nav

import kotlinx.serialization.Serializable

@Serializable
sealed class Route {
  @Serializable
  data object Rooms : Route()

  @Serializable
  data class Chat(val roomName: String) : Route()

  @Serializable
  data object Contacts : Route()

  @Serializable
  data object Profile : Route()

  @Serializable
  data class Contact(val nickName: String) : Route()
}
