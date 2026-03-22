package com.crosschain.assettracker.domain.repository

import com.crosschain.assettracker.domain.model.BalanceInfo
import kotlinx.coroutines.flow.Flow

interface BalanceRepository {
    fun getRealTimeBalance(): Flow<BalanceInfo>
}
