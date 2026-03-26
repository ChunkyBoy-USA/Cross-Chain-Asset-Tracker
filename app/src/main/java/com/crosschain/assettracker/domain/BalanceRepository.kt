package com.crosschain.assettracker.domain

import com.crosschain.assettracker.domain.model.BalanceInfo
import com.crosschain.assettracker.domain.model.Chain
import kotlinx.coroutines.flow.Flow

interface BalanceRepository {
    fun getTokenBalance(chain: Chain): Flow<BalanceInfo>
}
