package com.giraso.giraso.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.giraso.giraso.viewmodel.RoomViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RoomList(onJoin: (String) -> Unit) {
  val roomVm: RoomViewModel = koinViewModel()
  var input by remember { mutableStateOf("") }

  Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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