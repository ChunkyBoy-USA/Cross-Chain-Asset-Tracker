package com.crosschain.assettracker.domain.model

import com.crosschain.assettracker.data.local.database.entity.CcipSentRequestEntity
import com.crosschain.assettracker.data.model.ExecutionState

enum class TransferStatus {
    SUCCESS,
    FAILED,
    SENT,
    SOURCE_FINALIZED,
    COMMITTED,
    BLESSED,
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
    TransferStatus.SENT -> 0.1f
    TransferStatus.SOURCE_FINALIZED -> 0.2f
    TransferStatus.COMMITTED -> 0.5f
    TransferStatus.BLESSED -> 0.8f
    TransferStatus.SUCCESS -> 1f
    TransferStatus.FAILED -> 1f
}

fun ExecutionState.toTransferStatus() =
    when (this) {
        ExecutionState.SENT -> TransferStatus.SENT
        ExecutionState.SOURCE_FINALIZED -> TransferStatus.SOURCE_FINALIZED
        ExecutionState.COMMITTED -> TransferStatus.COMMITTED
        ExecutionState.BLESSED -> TransferStatus.BLESSED
        ExecutionState.SUCCESS -> TransferStatus.SUCCESS
        ExecutionState.FAILED -> TransferStatus.FAILED
    }
