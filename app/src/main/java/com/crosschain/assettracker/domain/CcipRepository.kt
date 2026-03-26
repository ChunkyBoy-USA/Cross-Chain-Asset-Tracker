package com.crosschain.assettracker.domain

import com.crosschain.assettracker.domain.model.CcipTransfer
import kotlinx.coroutines.flow.Flow

interface CcipRepository {
    fun trackTransfer(messageId: String): Flow<CcipTransfer>
}
