package com.crosschain.assettracker.domain.repository

import com.crosschain.assettracker.domain.model.CcipTransfer
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun loadAccounts()
}
