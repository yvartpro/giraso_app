package com.giraso.giraso.data.network.libp2p

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class Libp2pMessageMapper {
    private val json = Json { ignoreUnknownKeys = true }

    fun serialize(message: Libp2pMessage): ByteArray {
        return json.encodeToString(message).encodeToByteArray()
    }

    fun deserialize(data: ByteArray): Libp2pMessage? {
        return try {
            json.decodeFromString<Libp2pMessage>(data.decodeToString())
        } catch (e: Exception) {
            null
        }
    }
}
