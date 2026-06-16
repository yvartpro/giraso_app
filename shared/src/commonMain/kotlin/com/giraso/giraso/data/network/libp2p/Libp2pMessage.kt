package com.giraso.giraso.data.network.libp2p

import kotlinx.serialization.Serializable

@Serializable
data class Libp2pMessage(
    val room: String,
    val from: String,
    val text: String,
    val timestamp: Long
)
