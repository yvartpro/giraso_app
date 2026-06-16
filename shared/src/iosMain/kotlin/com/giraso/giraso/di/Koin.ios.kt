package com.giraso.giraso.di

import com.giraso.giraso.repo.ChatRepository
import com.giraso.giraso.domain.ChatMessage
import com.giraso.giraso.domain.Peer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.core.module.Module
import org.koin.dsl.module

class DummyChatRepository : ChatRepository {
    override fun sendMessage(room: String, text: String) {}
    override fun joinRoom(room: String) {}
    override fun leaveRoom(room: String) {}
    override fun setOnMessageListener(listener: (ChatMessage) -> Unit) {}
    override fun observeMessages(): Flow<ChatMessage> = emptyFlow()
    override fun observePeers(): Flow<List<Peer>> = emptyFlow()
    override fun whoami(): String = "ios-dummy"
}

actual val platformModule: Module = module {
    single<ChatRepository> { DummyChatRepository() }
}
