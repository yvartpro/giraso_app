package com.giraso.giraso.domain

data class ChatMessage(
    val id: String,
    val room: String,
    val from: String,
    val text: String,
    val timestamp: Long
)
