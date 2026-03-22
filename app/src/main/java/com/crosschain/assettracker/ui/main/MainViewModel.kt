package com.crosschain.assettracker.ui.main

import androidx.lifecycle.viewModelScope
import com.crosschain.assettracker.domain.model.Chain
import com.crosschain.assettracker.domain.repository.BalanceRepository
import com.crosschain.assettracker.domain.repository.CcipRepository
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
    private val balanceRepository: BalanceRepository,
    private val ccipRepository: CcipRepository
) : MviViewModel<MainUiState, MainIntent, MainSideEffect>() {

    override fun createInitialState(): MainUiState = MainUiState()

    override fun handleIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.LoadData -> {
                observeBalance()
            }
            is MainIntent.TrackTransfer -> {
                trackCcipTransfer(intent.messageId)
            }
            is MainIntent.TransferTokens -> {

            }
        }
    }

    private fun observeBalance() {
        viewModelScope.launch {
            balanceRepository.getRealTimeBalance()
                .onStart { setState { copy(isLoading = true) } }
                .catch { e -> 
                    setState { copy(errorMessage = e.message, isLoading = false) }
                    setEffect(MainSideEffect.ShowToast("Error loading balance"))
                }
                .collect { balance ->
                    setState {
                        when (balance.chain) {
                            Chain.ETHEREUM -> {
                                copy(ethRebaseTokenBalanceInfo = balance, isLoading = false)
                            }
                            Chain.ARBITRUM -> {
                                copy(arbRebaseTokenBalanceInfo = balance, isLoading = false)
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
}





