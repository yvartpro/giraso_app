package com.giraso.giraso.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.giraso.giraso.ui.screen.ChatScreen
import com.giraso.giraso.ui.screen.RoomList

@Composable
fun AppNav() {
  val navController = rememberNavController()

  NavHost(
    navController = navController,
    startDestination = Route.Rooms
  ) {
    composable<Route.Rooms> {
      RoomList { room ->
          navController.navigate(Route.Chat(room))
        }
    }
    composable<Route.Chat> { backStackEntry ->
      val chatRoute: Route.Chat = backStackEntry.toRoute()
      ChatScreen(
        room = chatRoute.roomName,
        onBack = {
          navController.popBackStack()
        }
      )
    }
  }
}
