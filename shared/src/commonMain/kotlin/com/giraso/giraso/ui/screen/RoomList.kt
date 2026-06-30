package com.giraso.giraso.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.giraso.giraso.viewmodel.ChatViewModel
import com.giraso.giraso.viewmodel.RoomViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomList(
  onJoin: (String) -> Unit
) {

  val roomVm: RoomViewModel = koinViewModel()
  val chatVm: ChatViewModel = koinViewModel()

  val rooms by roomVm.roomsFlow().collectAsState()

  var showCreateDialog by remember {
    mutableStateOf(false)
  }

  Scaffold(
    contentWindowInsets = WindowInsets.safeDrawing,
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text("Giraso")
        }
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          showCreateDialog = true
        }
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Create room"
        )
      }
    }
  ) { padding ->

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .windowInsetsPadding(WindowInsets.navigationBars),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(16.dp)
    ) {

      item {

        PeerCard(
          peerId = chatVm.whoami()
        )

      }

      item {

        Text(
          text = "Rooms",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold
        )

      }

      if (rooms.isEmpty()) {

        item {

          EmptyRooms()

        }

      } else {

        items(
          items = rooms,
          key = { it }
        ) { room ->

          RoomCard(
            room = room,
            onClick = {
              onJoin(room)
            }
          )

        }

      }

    }

  }

  if (showCreateDialog) {

    var roomName by remember {
      mutableStateOf("")
    }

    AlertDialog(
      onDismissRequest = {
        showCreateDialog = false
      },
      title = {
        Text("Create Room")
      },
      text = {

        OutlinedTextField(
          value = roomName,
          onValueChange = {
            roomName = it
          },
          modifier = Modifier.fillMaxWidth(),
          label = {
            Text("Room name")
          },
          singleLine = true
        )

      },
      confirmButton = {

        TextButton(
          onClick = {

            if (roomName.isNotBlank()) {

              roomVm.addRoom(roomName.trim())

              showCreateDialog = false

            }

          }
        ) {

          Text("Create")

        }

      },
      dismissButton = {

        TextButton(
          onClick = {

            showCreateDialog = false

          }
        ) {

          Text("Cancel")

        }

      }

    )

  }

}

@Composable
private fun PeerCard(
  peerId: String
) {

  ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.elevatedCardColors()
  ) {

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {

      Surface(
        modifier = Modifier
          .clip(CircleShape),
        color = MaterialTheme.colorScheme.primaryContainer
      ) {

        Box(
          modifier = Modifier.padding(14.dp),
          contentAlignment = Alignment.Center
        ) {

          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null
          )

        }

      }

      Spacer(modifier = Modifier.width(16.dp))

      Column {

        Text(
          text = "Your Peer ID",
          style = MaterialTheme.typography.labelMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = peerId,
          style = MaterialTheme.typography.bodyMedium
        )

      }

    }

  }

}

@Composable
private fun RoomCard(
  room: String,
  onClick: () -> Unit
) {

  ElevatedCard(
    onClick = onClick,
    modifier = Modifier.fillMaxWidth()
  ) {

    ListItem(

      leadingContent = {

        Icon(
          imageVector = Icons.Default.Groups,
          contentDescription = null
        )

      },

      headlineContent = {

        Text(room)

      },

      supportingContent = {

        Text("Tap to join")

      },

      trailingContent = {

        Icon(
          imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
          contentDescription = null
        )

      }

    )

  }

}

@Composable
private fun EmptyRooms() {

  ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp)
  ) {

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(32.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {

      Icon(
        imageVector = Icons.Default.Groups,
        contentDescription = null
      )

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        "No rooms yet",
        style = MaterialTheme.typography.titleMedium
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        "Tap the + button to create your first room.",
        style = MaterialTheme.typography.bodyMedium
      )

    }

  }

}
