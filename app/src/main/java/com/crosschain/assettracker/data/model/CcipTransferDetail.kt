package com.crosschain.assettracker.data.model

data class CcipTransferDetail(
    val messageId: String,
    val status: ExecutionState
)

enum class ExecutionState {
    UNTOUCHED,
    IN_PROGRESS,
    SUCCESS,
    FAILURE
}