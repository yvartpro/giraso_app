package com.giraso.giraso.di

import com.giraso.giraso.repo.ChatRepository
import com.giraso.giraso.transport.ChatTransport
import com.giraso.giraso.transport.FakeChatTransport
import com.giraso.giraso.viewmodel.ChatViewModel
import com.giraso.giraso.viewmodel.RoomViewModel
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

val appModule = module {
  single<ChatTransport> { FakeChatTransport() }
  single { ChatRepository(get()) }
  viewModel { ChatViewModel(get()) }
  viewModel { RoomViewModel() }
}
