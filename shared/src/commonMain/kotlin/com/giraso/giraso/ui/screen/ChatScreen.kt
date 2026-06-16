package com.giraso.giraso.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.giraso.giraso.viewmodel.ChatViewModel
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun ChatScreen(room: String) {
  val chatVm: ChatViewModel = koinViewModel()

  val messages by chatVm.messages(room).collectAsState()
  var input by remember { mutableStateOf("") }

  Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    Text("Room: $room", style = MaterialTheme.typography.titleLarge)
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