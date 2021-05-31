package org.adblockplus.adblockplussbrowser.preferences.ui.othersubscriptions

sealed class UiState {
    object Done : UiState()
    object Error : UiState()
    object Loading : UiState()
}