package com.giraso.giraso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.giraso.giraso.repo.ChatRepository
import com.giraso.giraso.transport.ChatTransport
import com.giraso.giraso.transport.FakeChatTransport
import com.giraso.giraso.viewmodel.ChatViewModel
import com.giraso.giraso.viewmodel.RoomViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // start Koin
        if (org.koin.core.context.GlobalContext.getOrNull() == null) {
            val appModule = module {
                single<com.giraso.giraso.transport.ChatTransport> { FakeChatTransport() }
                single { ChatRepository(get()) }
                single { ChatViewModel(get()) }
                single { RoomViewModel() }
            }

            startKoin {
                androidContext(this@MainActivity)
                modules(appModule)
            }
        }

        setContent {
            val chatVm = koinInject<ChatViewModel>()
            val roomVm = koinInject<RoomViewModel>()

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppContent(chatVm, roomVm)
                }
            }
        }
    }
}

@Composable
fun AppContent(chatVm: ChatViewModel, roomVm: RoomViewModel) {
    var currentRoom by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        RoomList(roomVm = roomVm, onJoin = { r -> currentRoom = r })
        Spacer(modifier = Modifier.height(12.dp))
        currentRoom?.let { room ->
            ChatScreen(room = room, chatVm = chatVm)
        }
    }
}

@Composable
fun RoomList(roomVm: RoomViewModel, onJoin: (String) -> Unit) {
    var input by remember { mutableStateOf("") }
    Column {
        Row {
            BasicTextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                roomVm.addRoom(input)
                input = ""
            }) { Text("Add") }
        }
        Spacer(modifier = Modifier.height(8.dp))
        val rooms by roomVm.roomsFlow().collectAsState()
        LazyColumn {
            items(rooms.size) { i ->
                val r = rooms[i]
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Text(r, modifier = Modifier.weight(1f))
                    Button(onClick = { onJoin(r) }) { Text("Join") }
                }
            }
        }
    }
}

@Composable
fun ChatScreen(room: String, chatVm: ChatViewModel) {
    val messages by chatVm.messages(room).collectAsState()
    var input by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Room: $room", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages.size) { i ->
                val m = messages[i]
                Text("${m.from}: ${m.text}")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            BasicTextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                chatVm.send(room, input)
                input = ""
            }) { Text("Send") }
        }
    }
}