package com.crosschain.assettracker.domain.model

import com.crosschain.assettracker.data.local.database.entity.CcipSentRequestEntity
import com.crosschain.assettracker.data.model.ExecutionState

enum class TransferStatus {
    INITIATED,
    WAITING_FOR_FINALITY,
    SUCCESS,
    FAILED
}

data class CcipTransfer(
    val requestId: Long,
    val sessionTopic: String,
    val method: String,
    val params: String,
    val chainId: String,
    val offRampAddress: String,
    val status: TransferStatus,
    val sourceChainName: String,
    val destinationChainName: String,
    val txHash: String? = null,
    val ccipMessageId: String? = null,
    val sequenceNumber: String? = null,
)

fun CcipSentRequestEntity.toCcipTransfer() = CcipTransfer(
    requestId = requestId,
    sessionTopic = sessionTopic,
    method = method,
    params = params,
    chainId = chainId,
    txHash = txHash,
    ccipMessageId = ccipMessageId,
    sequenceNumber = sequenceNumber,
    status = status,
    sourceChainName = sourceChainName,
    destinationChainName = destinationChainName,
    offRampAddress = offRampAddress,
)

fun CcipTransfer.toCcipSentRequestEntity() = CcipSentRequestEntity(
    requestId = requestId,
    sessionTopic = sessionTopic,
    method = method,
    params = params,
    chainId = chainId,
    txHash = txHash,
    ccipMessageId = ccipMessageId,
    sequenceNumber = sequenceNumber,
    status = status,
    sourceChainName = sourceChainName,
    destinationChainName = destinationChainName,
    offRampAddress = offRampAddress,
)

fun CcipTransfer.statusToProgress(): Float = when (status) {
    TransferStatus.INITIATED -> 0f
    TransferStatus.WAITING_FOR_FINALITY -> 0.5f
    TransferStatus.SUCCESS -> 1f
    TransferStatus.FAILED -> 1f
}

fun ExecutionState.toTransferStatus() =
    when (this) {
        ExecutionState.UNTOUCHED -> TransferStatus.INITIATED
        ExecutionState.IN_PROGRESS -> TransferStatus.WAITING_FOR_FINALITY
        ExecutionState.SUCCESS -> TransferStatus.SUCCESS
        ExecutionState.FAILURE -> TransferStatus.FAILED
    }

