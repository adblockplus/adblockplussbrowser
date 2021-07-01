package org.adblockplus.adblockplussbrowser.base.data.model

sealed class UpdateStatus {
    object Completed : UpdateStatus()
    data class Progress(val progress: Int) : UpdateStatus()
    object Error : UpdateStatus()
    object None : UpdateStatus()
}