package com.giraso.giraso.domain

data class Peer(
    val id: String,
    val nickname: String? = null,
    val isOnline: Boolean = true
)
