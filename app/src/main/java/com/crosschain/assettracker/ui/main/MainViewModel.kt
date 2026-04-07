package com.crosschain.assettracker.ui.main

import androidx.lifecycle.viewModelScope
import com.crosschain.assettracker.constants.AccountConstants
import com.crosschain.assettracker.data.local.EncryptedDataRepository
import com.crosschain.assettracker.domain.model.Chain
import com.crosschain.assettracker.domain.AccountRepository
import com.crosschain.assettracker.domain.RebaseTokenRepository
import com.crosschain.assettracker.domain.CcipRepository
import com.crosschain.assettracker.domain.model.PendingTransactionType
import com.crosschain.assettracker.domain.model.nameToChain
import com.crosschain.assettracker.domain.model.toTransferStatus
import com.crosschain.assettracker.ui.mvi.MviViewModel
import com.crosschain.assettracker.ui.mvi.main.MainIntent
import com.crosschain.assettracker.ui.mvi.main.MainSideEffect
import com.crosschain.assettracker.ui.mvi.main.MainUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigInteger
import javax.inject.Inject
import kotlin.text.toBigInteger

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

            is MainIntent.DeleteTransfer -> {
                deleteTransfer(intent.requestId)
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
                .onStart {
                    setState { copy(isLoading = true) }
                }
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

    private fun ccipTransfer(
        sourceChain: Chain,
        destinationChain: Chain,
        amountToSend: BigInteger,
        accountAddress: String
    ) {
        viewModelScope.launch {
            var ccipFeeAllowanceRequestId: Long? = null
            var amountToSendAllowanceRequestId: Long? = null
            val ccipFee = ccipRepository.getCcipFee(
                accountAddress,
                sourceChain,
                destinationChain,
                amountToSend
            )
            val ccipFeeWithBuffer = ccipFee.add(
                ccipFee.toBigDecimal().movePointLeft(1).toBigInteger()
            ) // Give 10% buffer

            val linkAllowance = ccipRepository.getRouterAllowance(
                sourceChain.ccipRouterAddress,
                sourceChain.linkTokenAddress,
                accountAddress,
                sourceChain.chainId
            )

            if (linkAllowance == null ||
                linkAllowance.allowance == null ||
                linkAllowance.allowance.toBigInteger() < ccipFeeWithBuffer
            ) {
                ccipFeeAllowanceRequestId = ccipRepository.approveRouterToSpend(
                    sourceChain,
                    accountAddress,
                    sourceChain.ccipRouterAddress,
                    sourceChain.linkTokenAddress,
                    ccipFeeWithBuffer
                )
                if (ccipFeeAllowanceRequestId == null) {
                    setEffect(MainSideEffect.ShowToast("Fail to approve routerAddress to spend Link Token"))
                    return@launch
                }
            }

            if (ccipFeeAllowanceRequestId != null) {
                val ccipFeeAllowanceFlow = ccipRepository.getRouterAllowanceFlow(
                    sourceChain.ccipRouterAddress,
                    sourceChain.linkTokenAddress,
                    accountAddress,
                    sourceChain.chainId
                )

                ccipFeeAllowanceFlow.filter {
                    (it?.allowance?.toBigInteger()
                        ?: BigInteger.ZERO) >= ccipFeeWithBuffer
                }.take(1).collect {
                    Timber.tag(TAG).d("ccip fee $ccipFeeWithBuffer approved, request id: $ccipFeeAllowanceRequestId")
                }
            }

            val rebaseTokenAllowance = ccipRepository.getRouterAllowance(
                sourceChain.ccipRouterAddress,
                sourceChain.rebaseTokenAddress,
                accountAddress,
                sourceChain.chainId
            )
            if (rebaseTokenAllowance == null ||
                rebaseTokenAllowance.allowance == null ||
                rebaseTokenAllowance.allowance.toBigInteger() < amountToSend
            ) {
                amountToSendAllowanceRequestId = ccipRepository.approveRouterToSpend(
                    sourceChain,
                    accountAddress,
                    sourceChain.ccipRouterAddress,
                    sourceChain.rebaseTokenAddress,
                    amountToSend
                )
                if (amountToSendAllowanceRequestId == null) {
                    setEffect(MainSideEffect.ShowToast("Fail to approve routerAddress to spend Rebase Token"))
                    return@launch
                }
            }

            if (amountToSendAllowanceRequestId != null) {
                val amountToSendAllowanceFlow = ccipRepository.getRouterAllowanceFlow(
                    sourceChain.ccipRouterAddress,
                    sourceChain.rebaseTokenAddress,
                    accountAddress,
                    sourceChain.chainId
                )

                amountToSendAllowanceFlow.filter {
                    (it?.allowance?.toBigInteger()
                        ?: BigInteger.ZERO) >= amountToSend
                }.take(1).collect {
                    Timber.tag(TAG).d("amountToSend $amountToSend approved, request id: $amountToSendAllowanceRequestId")
                }
            }

            val sentSucceed = ccipRepository.sendRebaseToken(
                accountAddress,
                sourceChain,
                destinationChain,
                amountToSend
            ).first()

            Timber.tag(TAG).d("sendRebaseToken() sentSucceed: $sentSucceed")
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
                    setState {
                        copy(
                            isLoading = false,
                            shouldConnectWallet = false,
                            ethRebaseTokenBalanceInfo = null,
                            arbRebaseTokenBalanceInfo = null
                        )
                    }
                } else {
                    setState {
                        copy(
                            isError = true,
                            errorMessage = "Fail to load account",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    private fun loadAccountFromCache() {
        viewModelScope.launch {
            val address = encryptedDataRepository.getString(AccountConstants.ACCOUNT_ADDRESS_PREF_KEY)
            if (address.isNullOrBlank()) {
                setState { copy(shouldConnectWallet = true) }
                return@launch
            }

            launch(Dispatchers.IO) {
                ccipRepository.getCcipTransfer().collect {
                    if (it != null) {
                        setState { copy(ccipTransfer = it) }
                        if (!it.txHash.isNullOrBlank() && (it.ccipMessageId.isNullOrBlank() || it.sequenceNumber.isNullOrBlank())) {
                            setState { copy(isLoading = true) }
                            ccipRepository.retrieveCcipMessageIdAndSequenceNumber(it.txHash, it.sourceChainName.nameToChain())
                            setState { copy(isLoading = false) }
                        } else if (it.ccipMessageId != null) {
                            setState { copy(isLoading = true) }
                            val result = ccipRepository.waitForCcipTransfer(
                                it.sourceChainName.nameToChain(),
                                it.destinationChainName.nameToChain(),
                                it.ccipMessageId
                            )
                            setState { copy(
                                isLoading = false,
                                ccipTransfer = ccipTransfer?.copy(status = result.toTransferStatus())
                            ) }
                        }
                    }
                }
            }

            launch {
                ccipRepository.getPendingTransaction().collect {
                    Timber.tag(TAG).d("Pending Transaction: $it")
                    if (it != null && it.txHash != null) {
                        when(it.type) {
                            PendingTransactionType.CCIP_REQUEST -> {
                                ccipRepository.insertCcipTransferTxHash(it.requestId, it.txHash)
                            }
                            PendingTransactionType.ROUTER_ALLOWANCE_REQUEST -> {
                                ccipRepository.insertPendingRouterAllowanceTxHash(it.requestId, it.txHash)
                            }
                        }
                        ccipRepository.deletePendingTransaction(it.requestId)
                    }
                }
            }

            launch {
                ccipRepository.getPendingRouterAllowanceFlow().collect {
                    Timber.tag(TAG).d("Pending Router Allowance: $it")
                    if (it!= null && it.txHash != null && !it.isApproved) {
                        val approved = ccipRepository.waitForPendingRouterAllowanceApproved(it.txHash, it.rpcUrl)
                        Timber.tag(TAG).d("Check Pending Router Allowance Approved, approved = $approved")
                        if (approved) {
                            ccipRepository.routerAllowanceApproved(it.routerAddress, it.tokenAddress, it.walletAddress, it.chainId, it.pendingAllowance)
                            ccipRepository.deletePendingRouterAllowance(it.requestId)
                        }
                    }
                }
            }
        }
    }

    private fun deleteTransfer(requestId: String) {
        viewModelScope.launch {
            ccipRepository.deleteCcipTransfer(requestId)
            ccipRepository.deletePendingRouterAllowance(requestId)
            ccipRepository.deletePendingTransaction(requestId)
            ccipRepository.deleteAllRouterAllowance()
        }
    }

    companion object {
        const val TAG = "MainViewModel"
    }
}





