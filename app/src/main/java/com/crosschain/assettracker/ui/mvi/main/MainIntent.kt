package com.crosschain.assettracker.ui.mvi.main

import com.crosschain.assettracker.domain.model.Chain

sealed class MainIntent {
    data class LoadBalance(val chain: Chain) : MainIntent()
    data class TrackTransfer(val messageId: String) : MainIntent()
    data class TransferTokens(val amount: Long, val sourceChain: Chain, val destinationChain: Chain) : MainIntent()

    data object LoadAccountFromWallet : MainIntent()
    data object LoadAccountFromCache : MainIntent()
}
