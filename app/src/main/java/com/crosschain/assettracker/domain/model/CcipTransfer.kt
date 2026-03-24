package com.crosschain.assettracker.domain.model

enum class TransferStatus {
    INITIATED,
    WAITING_FOR_FINALITY,
    SUCCESS,
    FAILED
}

data class CcipTransfer(
    val messageId: String,
    val sourceTransactionHash: String,
    val destinationTransactionHash: String,
    val sourceChain: String,
    val destinationChain: String,
    val status: TransferStatus,
    val progress: Float,
    val fees: String,
)
