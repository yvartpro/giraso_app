package com.giraso.giraso.data.network.libp2p

import android.content.Context
import com.giraso.giraso.utils.Logger
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
  companion object {
    private const val TAG = "Libp2pClient"
  }

  private var host: Host? = null
  private var gossip: Gossip? = null
  private val scope = CoroutineScope(Dispatchers.IO + Job())
  
  private var sentCount = 0
  private var receivedCount = 0

  private val _messages = MutableSharedFlow<Libp2pMessage>(extraBufferCapacity = 100)
  val messages: SharedFlow<Libp2pMessage> = _messages.asSharedFlow()

  private val _peers = MutableSharedFlow<List<String>>(replay = 1)
  val peers: SharedFlow<List<String>> = _peers.asSharedFlow()

  private val subscriptions = mutableMapOf<String, PubsubSubscription>()

  fun start() {
    Logger.d(TAG, "[START] beginning libp2p initialization")
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
        listen("/ip4/127.0.0.1/tcp/0")
      }
    }.also { h ->
      Logger.d(TAG, "[START] host dsl built, starting...")
      h.start().join()
      val fullPeerId = h.peerId.toBase58()
      val listenAddrs = h.listenAddresses().joinToString(", ")
      Logger.i(TAG, "[START] ✓ HOST STARTED")
      Logger.i(TAG, "[START] PeerID: $fullPeerId")
      Logger.i(TAG, "[START] Listening on: $listenAddrs")

      var mdns: MDnsDiscovery? = null
      try {
        mdns = MDnsDiscovery(h, "_libp2p._udp.local.", 1000)
        mdns.start().join()
        Logger.d(TAG, "[START] mDNS discovery started")
      } catch (e: Exception) {
        Logger.w(TAG, "[START] mDNS failed: ${e.message}")
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
    Logger.d(TAG, "[STOP] shutting down host and subscriptions")
    subscriptions.forEach { (room, _) ->
      Logger.d(TAG, "[STOP] unsubscribing from $room")
    }
    subscriptions.clear()
    host?.stop()?.join()
    Logger.i(TAG, "[STOP] ✓ HOST STOPPED")
  }

  fun whoami(): String {
    return host?.peerId?.toBase58() ?: "unknown"
  }

  fun getListenAddresses(): List<String> {
    return host?.listenAddresses()?.map { it.toString() } ?: emptyList()
  }

  fun dialAddress(multiaddr: String) {
    try {
      Logger.d(TAG, "[PEER_DIAL] attempting $multiaddr via reflection (string)")
      val network = host?.network ?: run {
        Logger.w(TAG, "[PEER_DIAL] no network available")
        return
      }
      // enumerate candidate methods and try invoking with the raw string
      var attempted = false
      for (m in network.javaClass.methods) {
        if (m.parameterCount == 1) {
          val pt = m.parameterTypes[0]
          try {
            // If method accepts a String-ish param, try invoking directly
            if (pt == java.lang.String::class.java || pt == CharSequence::class.java || pt == Any::class.java) {
              Logger.d(TAG, "[PEER_DIAL] trying ${m.name}(${pt.simpleName})")
              m.invoke(network, multiaddr)
              Logger.i(TAG, "[PEER_DIAL] invoked ${m.name} with string")
              return
            }

            // If method expects an array of Multiaddr, build one reflectively
            if (pt.isArray && pt.componentType.name.contains("Multiaddr")) {
              Logger.d(TAG, "[PEER_DIAL] ${m.name} expects ${pt.componentType.name}[] — attempting to construct Multiaddr instance")
              val compType = pt.componentType
              var addrInstance: Any? = null

              // Try common factory/constructor names
              try {
                // static parse/fromString/valueOf
                val parseMethods = listOf("fromString", "valueOf", "parse", "of")
                for (name in parseMethods) {
                  try {
                    val method = compType.getMethod(name, String::class.java)
                    addrInstance = method.invoke(null, multiaddr)
                    break
                  } catch (_: Throwable) {
                  }
                }
              } catch (_: Throwable) {
              }

              if (addrInstance == null) {
                try {
                  val ctor = compType.getConstructor(String::class.java)
                  addrInstance = ctor.newInstance(multiaddr)
                } catch (_: Throwable) {
                }
              }

              if (addrInstance != null) {
                val arr = java.lang.reflect.Array.newInstance(compType, 1)
                java.lang.reflect.Array.set(arr, 0, addrInstance)
                m.invoke(network, arr)
                Logger.i(TAG, "[PEER_DIAL] invoked ${m.name} with Multiaddr[]")
                return
              } else {
                Logger.d(TAG, "[PEER_DIAL] couldn't construct Multiaddr instance for ${compType.name}")
              }
            }
          } catch (ex: Throwable) {
            attempted = true
            Logger.d(TAG, "[PEER_DIAL] ${m.name} failed: ${ex.message}")
          }
        }
      }
      if (!attempted) Logger.w(TAG, "[PEER_DIAL] no candidate methods to dial $multiaddr")
    } catch (e: Exception) {
      Logger.e(TAG, "[PEER_DIAL] reflection failed: ${e.message}", e)
    }
  }

  fun joinRoom(room: String) {
    if (subscriptions.containsKey(room)) {
      Logger.d(TAG, "[JOIN_ROOM] already subscribed to $room")
      return
    }
    Logger.d(TAG, "[JOIN_ROOM] subscribing to room=$room")

    val sub = gossip?.subscribe(Consumer { msg ->
      val mapper = Libp2pMessageMapper()
      val data = msg.data
      val bytes = ByteArray(data.readableBytes())
      data.getBytes(data.readerIndex(), bytes)
      mapper.deserialize(bytes)?.let { libp2pMsg ->
        receivedCount++
        Logger.i(TAG, "[MESSAGE_RECEIVED] ✓ peerId=${whoami().take(8)}... topic=${libp2pMsg.room} from=${libp2pMsg.from.take(8)}... text='${libp2pMsg.text}' (received: $receivedCount)")
        scope.launch { _messages.emit(libp2pMsg) }
      }
    }, Topic(room))

    if (sub != null) {
      subscriptions[room] = sub
      Logger.i(TAG, "[JOIN_ROOM] ✓ SUBSCRIBED room=$room")
    } else {
      Logger.e(TAG, "[JOIN_ROOM] failed to subscribe to $room")
    }
  }

  fun leaveRoom(room: String) {
    subscriptions.remove(room)?.unsubscribe()
  }

  fun sendMessage(room: String, text: String) {
    sentCount++
    val myId = whoami().take(8)
    Logger.d(TAG, "[MESSAGE_SEND] publishing peerId=$myId topic=$room text='$text'")
    val msg = Libp2pMessage(
      room = room,
      from = whoami(),
      text = text,
      timestamp = System.currentTimeMillis()
    )
    val data = Libp2pMessageMapper().serialize(msg)
    val buf = Unpooled.wrappedBuffer(data)
    gossip?.createPublisher(null, System.currentTimeMillis())?.publish(buf, Topic(room))
    Logger.i(TAG, "[MESSAGE_SEND] ✓ PUBLISHED peerId=$myId topic=$room text='$text' (sent: $sentCount)")
  }

  private fun updatePeers() {
    val connectedPeers = host?.network?.connections?.map { it.secureSession().remoteId.toBase58() }?.distinct() ?: emptyList()
    if (connectedPeers.isNotEmpty()) {
      connectedPeers.forEach { peerId ->
        Logger.d(TAG, "[PEER_CONNECTED] peerId=${peerId.take(8)}...")
      }
    }
    scope.launch { _peers.emit(connectedPeers) }
  }

  fun getStats(): Pair<Int, Int> = Pair(sentCount, receivedCount)

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
