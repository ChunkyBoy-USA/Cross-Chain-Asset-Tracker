package com.crosschain.assettracker.data.local.database.entity

import androidx.room.Entity
import com.crosschain.assettracker.domain.model.PendingTransactionType

@Entity(
    tableName = "pending_transaction",
    primaryKeys = ["requestId"]
)
data class PendingTransactionEntity(
    val requestId: String,
    val type: PendingTransactionType,
    val txHash: String?
)