package com.crosschain.assettracker.data.local.database.entity

import androidx.room.Entity
import com.crosschain.assettracker.domain.model.TransferStatus

@Entity(
    tableName = "ccip_sent_request",
    primaryKeys = ["sessionTopic", "requestId"]
)
data class CcipSentRequestEntity(
    val requestId: Long,
    val sessionTopic: String,
    val method: String,
    val params: String,
    val chainId: String,
    val status: TransferStatus,
    val txHash: String?,
    val ccipMessageId: String?,
    val sourceChain: String,
    val destinationChain: String
)

