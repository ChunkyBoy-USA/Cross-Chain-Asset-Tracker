package com.crosschain.assettracker.ui.main

import androidx.lifecycle.viewModelScope
import com.crosschain.assettracker.constants.AccountConstants
import com.crosschain.assettracker.data.local.EncryptedDataRepository
import com.crosschain.assettracker.domain.model.Chain
import com.crosschain.assettracker.domain.AccountRepository
import com.crosschain.assettracker.domain.RebaseTokenRepository
import com.crosschain.assettracker.domain.CcipRepository
import com.crosschain.assettracker.ui.mvi.MviViewModel
import com.crosschain.assettracker.ui.mvi.main.MainIntent
import com.crosschain.assettracker.ui.mvi.main.MainSideEffect
import com.crosschain.assettracker.ui.mvi.main.MainUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val rebaseTokenRepository: RebaseTokenRepository,
    private val ccipRepository: CcipRepository,
    private val encryptedDataRepository: EncryptedDataRepository,
    private val accountRepository: AccountRepository,
) : MviViewModel<MainUiState, MainIntent, MainSideEffect>() {

    override fun createInitialState(): MainUiState = MainUiState()

    override fun handleIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.LoadBalance -> {
                observeBalance(intent.chain)
            }

            is MainIntent.TrackTransfer -> {
                trackCcipTransfer(intent.messageId)
            }

            is MainIntent.TransferTokens -> {

            }

            is MainIntent.LoadAccountFromWallet -> {
                loadAccountFromWallet()
            }

            is MainIntent.LoadAccountFromCache -> {
                loadAccountFromCache()
            }
        }
    }

    private fun observeBalance(chain: Chain) {
        val address = accountRepository.getCurrentAccountAddress()

        if (address == null) {
            setState {
                copy(
                    shouldConnectWallet = true,
                    isLoading = false,
                    isError = false,
                    errorMessage = null,
                    ethRebaseTokenBalanceInfo = null,
                    arbRebaseTokenBalanceInfo = null,
                    ccipTransfer = null
                )
            }
        } else {
            viewModelScope.launch {
                rebaseTokenRepository.getTokenBalance(chain, address)
                    .onStart { setState { copy(isLoading = true) } }
                    .catch { e ->
                        setState { copy(errorMessage = e.message, isLoading = false) }
                        setEffect(MainSideEffect.ShowToast("Error loading balance"))
                    }
                    .collect { balance ->
                        setState {
                            when (balance.chain) {
                                Chain.ETHEREUM -> {
                                    copy(ethRebaseTokenBalanceInfo = balance, isLoading = arbRebaseTokenBalanceInfo == null)
                                }
                                Chain.ARBITRUM -> {
                                    copy(arbRebaseTokenBalanceInfo = balance, isLoading = ethRebaseTokenBalanceInfo == null)
                                }
                            }
                        }
                    }
            }
        }
    }

    private fun trackCcipTransfer(messageId: String) {
        viewModelScope.launch {
            ccipRepository.trackTransfer(messageId)
                .collect { transfer ->
                    setState { copy(ccipTransfer = transfer) }
                }
        }
    }

    private fun loadAccountFromWallet() {
        viewModelScope.launch {
            accountRepository.loadAccountFromAppKit().catch { e ->
                setState { copy(errorMessage = e.message, isLoading = false) }
                setEffect(MainSideEffect.ShowToast("Error loading account"))
            }.collect { isSuccessful ->
                if (isSuccessful) {
                    setState { copy(shouldConnectWallet = false) }
                } else {
                    setState { copy(isError = true, errorMessage = "Fail to load account") }
                }
            }
        }
    }

    private fun loadAccountFromCache() {
        viewModelScope.launch {
            val address =
                encryptedDataRepository.getString(AccountConstants.ACCOUNT_ADDRESS_PREF_KEY)
            if (address.isNullOrBlank()) {
                setState { copy(shouldConnectWallet = true) }
            }
        }
    }
}





