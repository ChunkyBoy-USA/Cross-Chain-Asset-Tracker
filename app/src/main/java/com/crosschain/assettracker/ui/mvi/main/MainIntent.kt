package com.crosschain.assettracker.ui.mvi.main

import com.crosschain.assettracker.domain.model.Chain

sealed class MainIntent {
    data class LoadData(val chain: Chain) : MainIntent()
    data class TrackTransfer(val messageId: String) : MainIntent()
    data class TransferTokens(val amount: String, val fromAddress: String, val toAddress: String) : MainIntent()

    data object LoadAccountFromWallet : MainIntent()
    data object LoadAccountFromLocal : MainIntent()
}
