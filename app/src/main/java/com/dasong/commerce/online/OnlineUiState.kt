package com.dasong.commerce.online

data class OnlineUiState(
    val playerName: String = "",
    val roomCodeInput: String = "",
    val currentRoom: RoomData? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val shouldNavigateToGame: Boolean = false,
)
