package com.crosschain.assettracker.data.repository

import com.crosschain.assettracker.domain.model.CcipTransfer
import com.crosschain.assettracker.domain.repository.CcipRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CcipRepositoryImpl @Inject constructor() : CcipRepository{
    override fun trackTransfer(messageId: String): Flow<CcipTransfer> {
        TODO("Not yet implemented")
    }
}