package com.dasong.commerce.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dasong.commerce.ui.home.HomeScreen
import com.dasong.commerce.ui.guide.GuideScreen
import com.dasong.commerce.ui.setup.SetupScreen
import com.dasong.commerce.ui.room.RoomScreen
import com.dasong.commerce.ui.game.GameScreen
import com.dasong.commerce.ui.result.ResultScreen
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onStartGame = { navController.navigate("setup") },
                onGuide = { navController.navigate("guide") },
                onCreateRoom = { navController.navigate("room") },
                onJoinRoom = { navController.navigate("room") }
            )
        }

        composable("guide") {
            GuideScreen(onBack = { navController.popBackStack() })
        }

        composable("setup") {
            SetupScreen(
                onGameStart = { playerCount ->
                    navController.navigate("game/$playerCount") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("room") {
            RoomScreen(
                onBack = { navController.popBackStack() },
                onStartGame = { playerCount, playerNames ->
                    val encodedNames = URLEncoder.encode(
                        playerNames.joinToString(","),
                        "UTF-8"
                    )
                    navController.navigate("game/$playerCount/$encodedNames") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "game/{playerCount}/{playerNames}",
            arguments = listOf(
                navArgument("playerCount") { type = NavType.IntType },
                navArgument("playerNames") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val playerCount = backStackEntry.arguments?.getInt("playerCount") ?: 2
            val namesStr = backStackEntry.arguments?.getString("playerNames") ?: ""
            val playerNames = if (namesStr.isNotEmpty()) {
                URLDecoder.decode(namesStr, "UTF-8").split(",")
            } else {
                emptyList()
            }
            GameScreen(
                playerCount = playerCount,
                playerNames = playerNames,
                onGameEnd = { winnerName ->
                    navController.navigate("result/$winnerName") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "game/{playerCount}",
            arguments = listOf(navArgument("playerCount") { type = NavType.IntType })
        ) { backStackEntry ->
            val playerCount = backStackEntry.arguments?.getInt("playerCount") ?: 2
            GameScreen(
                playerCount = playerCount,
                playerNames = emptyList(),
                onGameEnd = { winnerName ->
                    navController.navigate("result/$winnerName") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "result/{winnerName}",
            arguments = listOf(navArgument("winnerName") { type = NavType.StringType })
        ) { backStackEntry ->
            val winnerName = backStackEntry.arguments?.getString("winnerName") ?: ""
            ResultScreen(
                winnerName = winnerName,
                onBackHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
