package com.giraso.giraso.di

import com.giraso.giraso.data.network.libp2p.Libp2pChatRepository
import com.giraso.giraso.data.network.libp2p.Libp2pClient
import com.giraso.giraso.repo.ChatRepository
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single(createdAtStart = true) { Libp2pClient(get()) }
    single<ChatRepository>(createdAtStart = true) { Libp2pChatRepository(get()) }
}
