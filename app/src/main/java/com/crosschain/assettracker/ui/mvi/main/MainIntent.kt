package com.crosschain.assettracker.ui.mvi.main

sealed class MainIntent {
    object LoadData : MainIntent()
    data class TrackTransfer(val messageId: String) : MainIntent()
    data class TransferTokens(val amount: String, val fromAddress: String, val toAddress: String) : MainIntent()
}
