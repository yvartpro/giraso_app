package com.giraso.giraso.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.giraso.giraso.viewmodel.ChatViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
  room: String,
  onBack: (() -> Unit)? = null
) {

  val chatVm: ChatViewModel = koinViewModel()

  val messages by chatVm.messages(room).collectAsState()

  var input by remember {
    mutableStateOf("")
  }

  val listState = rememberLazyListState()

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.lastIndex)
    }
  }

  Scaffold(
    contentWindowInsets = WindowInsets.safeDrawing,
    topBar = {

      CenterAlignedTopAppBar(

        navigationIcon = {

          if (onBack != null) {

            IconButton(
              onClick = onBack
            ) {

              Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
              )

            }

          }

        },

        title = {

          Column(
            horizontalAlignment = Alignment.CenterHorizontally
          ) {

            Text(
              room,
              fontWeight = FontWeight.SemiBold
            )

            Text(
              "Decentralized room",
              style = MaterialTheme.typography.labelSmall
            )

          }

        },

        actions = {

          Icon(
            imageVector = Icons.Default.Groups,
            contentDescription = null,
            modifier = Modifier.padding(end = 16.dp)
          )

        },

        colors = TopAppBarDefaults.centerAlignedTopAppBarColors()

      )

    },

    bottomBar = {

      Surface(
        tonalElevation = 3.dp
      ) {

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .navigationBarsPadding()
            .imePadding(),
          verticalAlignment = Alignment.Bottom
        ) {

          OutlinedTextField(
            value = input,
            onValueChange = {
              input = it
            },
            modifier = Modifier.weight(1f),
            placeholder = {
              Text("Message")
            },
            shape = RoundedCornerShape(24.dp),
            maxLines = 5
          )

          Spacer(modifier = Modifier.padding(horizontal = 6.dp))

          FilledIconButton(

            onClick = {

              if (input.isNotBlank()) {

                chatVm.send(
                  room,
                  input.trim()
                )

                input = ""

              }

            }

          ) {

            Icon(
              Icons.AutoMirrored.Filled.Send,
              contentDescription = "Send"
            )

          }

        }

      }

    }

  ) { padding ->

    if (messages.isEmpty()) {

      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding),
        contentAlignment = Alignment.Center
      ) {

        Column(
          horizontalAlignment = Alignment.CenterHorizontally
        ) {

          Icon(
            Icons.Default.Groups,
            contentDescription = null
          )

          Spacer(modifier = Modifier.padding(8.dp))

          Text(
            "No messages yet",
            style = MaterialTheme.typography.titleMedium
          )

          Text(
            "Start the conversation."
          )

        }

      }

    } else {

      LazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxSize()
          .padding(padding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {

        items(messages) { message ->

          val mine = message.from == chatVm.whoami()

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
              if (mine)
                Arrangement.End
              else
                Arrangement.Start
          ) {

            Surface(

              color =
                if (mine)
                  MaterialTheme.colorScheme.primaryContainer
                else
                  MaterialTheme.colorScheme.surfaceVariant,

              shape = RoundedCornerShape(20.dp),

              modifier = Modifier.fillMaxWidth(0.75f)

            ) {

              Column(
                modifier = Modifier.padding(14.dp)
              ) {

                if (!mine) {

                  Text(
                    text = message.from,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                  )

                  Spacer(modifier = Modifier.padding(2.dp))

                }

                Text(
                  text = message.text,
                  style = MaterialTheme.typography.bodyLarge
                )

              }

            }

          }

        }

      }

    }

  }

}
