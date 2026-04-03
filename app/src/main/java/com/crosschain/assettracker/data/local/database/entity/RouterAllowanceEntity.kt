package com.crosschain.assettracker.data.local.database.entity

import androidx.room.Entity

@Entity(
    tableName = "router_allowance",
    primaryKeys = ["routerAddress", "walletAddress", "tokenAddress", "chainId"]
)
data class RouterAllowanceEntity (
    val routerAddress: String,
    val walletAddress: String,
    val tokenAddress: String,
    val chainId: String,
    val allowance: String?
)