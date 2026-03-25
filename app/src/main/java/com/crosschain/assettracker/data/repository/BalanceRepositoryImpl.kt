package com.crosschain.assettracker.data.repository

import com.crosschain.assettracker.domain.model.BalanceInfo
import com.crosschain.assettracker.domain.model.Chain
import com.crosschain.assettracker.domain.repository.BalanceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BalanceRepositoryImpl @Inject constructor(): BalanceRepository {
    override fun getTokenBalance(chain: Chain): Flow<BalanceInfo> {
        TODO("Not yet implemented")
    }
}