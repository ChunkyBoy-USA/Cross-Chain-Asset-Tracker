package com.crosschain.assettracker.ui.mvi.main

import com.crosschain.assettracker.domain.model.BalanceInfo
import com.crosschain.assettracker.domain.model.CcipTransfer

data class MainUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val ethRebaseTokenBalanceInfo: BalanceInfo? = null,
    val arbRebaseTokenBalanceInfo: BalanceInfo? = null,
    val ccipTransfer: CcipTransfer? = null
)
