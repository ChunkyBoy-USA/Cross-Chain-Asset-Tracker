package com.crosschain.assettracker.data.local.database.entity

import androidx.room.Entity

@Entity(
    tableName = "pending_router_allowance",
    primaryKeys = ["requestId"]
)
data class PendingRouterAllowanceEntity (
    val routerAddress: String,
    val walletAddress: String,
    val tokenAddress: String,
    val chainId: String,
    val pendingAllowance: String,
    val requestId: String,
    val isApproved: Boolean
)