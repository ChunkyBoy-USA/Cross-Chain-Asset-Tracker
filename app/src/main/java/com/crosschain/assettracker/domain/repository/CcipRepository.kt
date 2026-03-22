package com.crosschain.assettracker.domain.repository

import com.crosschain.assettracker.domain.model.CcipTransfer
import kotlinx.coroutines.flow.Flow

interface CcipRepository {
    fun trackTransfer(messageId: String): Flow<CcipTransfer>
}
