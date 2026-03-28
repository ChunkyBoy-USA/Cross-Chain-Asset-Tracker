package com.crosschain.assettracker.domain

import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun loadAccountFromAppKit(): Flow<Boolean>
    fun getCurrentAccountAddress(): String?
}
