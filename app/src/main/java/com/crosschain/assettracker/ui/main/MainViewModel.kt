package com.crosschain.assettracker.ui.main

import android.util.Log
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
import com.reown.appkit.client.AppKit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigInteger
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
        Timber.tag(TAG).d("handleIntent, intent = $intent")
        when (intent) {
            is MainIntent.LoadBalance -> {
                val accountAddress = accountRepository.getCurrentAccountAddress() ?: ""
                observeBalance(intent.chain, accountAddress)
            }

            is MainIntent.TrackTransfer -> {
                trackCcipTransfer(intent.messageId)
            }

            is MainIntent.TransferTokens -> {
                val accountAddress = accountRepository.getCurrentAccountAddress() ?: ""
                ccipTransfer(
                    intent.sourceChain,
                    intent.destinationChain,
                    intent.amount,
                    accountAddress
                )
            }

            is MainIntent.LoadAccountFromWallet -> {
                loadAccountFromWallet()
            }

            is MainIntent.LoadAccountFromCache -> {
                loadAccountFromCache()
            }
        }
    }

    private fun observeBalance(chain: Chain, accountAddress: String) {
        viewModelScope.launch {
            rebaseTokenRepository.getTokenBalance(chain, accountAddress)
                .onStart { setState { copy(isLoading = true) } }
                .catch { e ->
                    setState { copy(errorMessage = e.message, isLoading = false) }
                    setEffect(MainSideEffect.ShowToast("Error loading balance"))
                }
                .collect { balance ->
                    setState {
                        when (balance.chain) {
                            Chain.ETHEREUM -> {
                                copy(
                                    ethRebaseTokenBalanceInfo = balance,
                                    isLoading = arbRebaseTokenBalanceInfo == null
                                )
                            }
                            Chain.ARBITRUM -> {
                                copy(
                                    arbRebaseTokenBalanceInfo = balance,
                                    isLoading = ethRebaseTokenBalanceInfo == null
                                )
                            }
                        }
                    }
                }
        }
    }

    private fun trackCcipTransfer(messageId: String) {
        viewModelScope.launch {
            ccipRepository.trackTransfer()
                .collect { transfer ->
                    setState { copy(ccipTransfer = transfer) }
                }
        }
    }

    private fun ccipTransfer(
        sourceChain: Chain,
        destinationChain: Chain,
        amountToSend: Long,
        accountAddress: String
    ) {
        viewModelScope.launch {
            ccipRepository.sendRebaseToken(
                accountAddress,
                sourceChain,
                destinationChain,
                BigInteger.valueOf(amountToSend)
            ).collect {
                it
            }
        }
    }

    private fun loadAccountFromWallet() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            accountRepository.loadAccountFromAppKit().catch { e ->
                setState { copy(errorMessage = e.message, isLoading = false) }
                setEffect(MainSideEffect.ShowToast("Error loading account"))
            }.collect { isSuccessful ->
                if (isSuccessful) {
                    setState { copy(isLoading = false, shouldConnectWallet = false, ethRebaseTokenBalanceInfo = null, arbRebaseTokenBalanceInfo = null) }
                } else {
                    setState { copy(isError = true, errorMessage = "Fail to load account", isLoading = false) }
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
                return@launch
            }

            ccipRepository.getPendingTransfer().collect {
                if (it != null) {
                    if (!it.txHash.isNullOrBlank() && it.ccipMessageId.isNullOrBlank()) {
                        setState { copy(isLoading = true) }
                        val chain = if (it.sourceChain == Chain.ETHEREUM.name) {
                            Chain.ETHEREUM
                        } else {
                            Chain.ARBITRUM
                        }
                        ccipRepository.getCcipMessageId(it.txHash, chain)
                        setState { copy(isLoading = false) }
                    } else {


                    }
                    setState { copy(ccipTransfer = it) }
                }
            }
        }
    }

    companion object {
        const val TAG = "MainViewModel"
    }
}





