package com.giraso.giraso.di

import com.giraso.giraso.viewmodel.ChatViewModel
import com.giraso.giraso.viewmodel.RoomViewModel
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

expect val platformModule: Module

val appModule = module {
  includes(platformModule)
  viewModel { ChatViewModel(get()) }
  viewModel { RoomViewModel() }
}
