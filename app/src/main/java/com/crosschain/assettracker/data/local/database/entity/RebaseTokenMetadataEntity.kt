package com.crosschain.assettracker.data.local.database.entity

import androidx.room.Entity

@Entity(
    tableName = "rebase_token_metadata",
    primaryKeys = ["chainId", "tokenAddress"]
)
data class RebaseTokenMetadataEntity(
    val chainId: String,
    val tokenAddress: String,
    val symbol: String,
    val decimals: Int,
    val baseInterestRate: String
)

