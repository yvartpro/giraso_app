package com.giraso.giraso.data.network.libp2p

import com.giraso.giraso.domain.Peer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class Libp2pPeerTracker(private val client: Libp2pClient) {
  fun observePeers(): Flow<List<Peer>> = client.peers.map { ids ->
    ids.map { Peer(id = it, nickname = "Peer-${it.take(8)}") }
  }
}
