package com.giraso.giraso.data.network.libp2p

import android.content.Context
import io.libp2p.core.Host
import io.libp2p.core.dsl.host
import io.libp2p.core.crypto.unmarshalPrivateKey
import io.libp2p.core.crypto.marshalPrivateKey
import io.libp2p.core.crypto.generateKeyPair
import io.libp2p.core.crypto.KeyType
import io.libp2p.core.crypto.PrivKey
import io.libp2p.core.pubsub.Topic
import io.libp2p.core.pubsub.PubsubSubscription
import io.libp2p.discovery.MDnsDiscovery
import io.libp2p.core.mux.StreamMuxerProtocol
import io.libp2p.pubsub.gossip.Gossip
import io.libp2p.security.noise.NoiseXXSecureChannel
import io.libp2p.transport.tcp.TcpTransport
import io.netty.buffer.Unpooled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.function.Consumer

class Libp2pClient(private val context: Context, private val idFileName: String = "libp2p_id.key") {
  private var host: Host? = null
  private var gossip: Gossip? = null
  private val scope = CoroutineScope(Dispatchers.IO + Job())

  private val _messages = MutableSharedFlow<Libp2pMessage>(extraBufferCapacity = 100)
  val messages: SharedFlow<Libp2pMessage> = _messages.asSharedFlow()

  private val _peers = MutableSharedFlow<List<String>>(replay = 1)
  val peers: SharedFlow<List<String>> = _peers.asSharedFlow()

  private val subscriptions = mutableMapOf<String, PubsubSubscription>()

  fun start() {
    val privKey = loadOrCreateKey()

    val gossipInstance = Gossip()
    this.gossip = gossipInstance

    host = host {
      identity {
        factory = { privKey }
      }
      transports {
        add(::TcpTransport)
      }
      secureChannels {
        add(::NoiseXXSecureChannel)
      }
      muxers {
        add(StreamMuxerProtocol.getYamux(1024, 256))
      }
      protocols {
        +gossipInstance
      }
      network {
        listen("/ip4/0.0.0.0/tcp/0")
      }
    }.also { h ->
      h.start().join()

      var mdns: MDnsDiscovery? = null
      try {
        mdns = MDnsDiscovery(h, "_libp2p._udp.local.", 1000)
        mdns.start()
      } catch (e: Exception) {
      }

      // use mDNS peer discovery callback to keep peers up-to-date
      try {
        mdns?.addHandler { updatePeers() }
      } catch (_: Throwable) {
      }
    }
    updatePeers()
  }

  fun stop() {
    host?.stop()?.join()
  }

  fun whoami(): String {
    return host?.peerId?.toBase58() ?: "unknown"
  }

  fun joinRoom(room: String) {
    if (subscriptions.containsKey(room)) return

    val sub = gossip?.subscribe(Consumer { msg ->
      val mapper = Libp2pMessageMapper()
      val data = msg.data
      val bytes = ByteArray(data.readableBytes())
      data.getBytes(data.readerIndex(), bytes)
      mapper.deserialize(bytes)?.let {
        scope.launch { _messages.emit(it) }
      }
    }, Topic(room))

    if (sub != null) {
      subscriptions[room] = sub
    }
  }

  fun leaveRoom(room: String) {
    subscriptions.remove(room)?.unsubscribe()
  }

  fun sendMessage(room: String, text: String) {
    val msg = Libp2pMessage(
      room = room,
      from = whoami(),
      text = text,
      timestamp = System.currentTimeMillis()
    )
    val data = Libp2pMessageMapper().serialize(msg)
    val buf = Unpooled.wrappedBuffer(data)
    gossip?.createPublisher(null, System.currentTimeMillis())?.publish(buf, Topic(room))
  }

  private fun updatePeers() {
    val connectedPeers = host?.network?.connections?.map { it.secureSession().remoteId.toBase58() }?.distinct() ?: emptyList()
    scope.launch { _peers.emit(connectedPeers) }
  }

  private fun loadOrCreateKey(): PrivKey {
    val keyFile = File(context.filesDir, idFileName)
    return if (keyFile.exists()) {
      unmarshalPrivateKey(keyFile.readBytes())
    } else {
      val pair = generateKeyPair(KeyType.ED25519)
      val priv = pair.first
      keyFile.writeBytes(marshalPrivateKey(priv))
      priv
    }
  }
}
