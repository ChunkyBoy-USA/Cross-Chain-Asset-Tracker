package com.crosschain.assettracker.ui.main

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crosschain.assettracker.domain.model.BalanceInfo
import com.crosschain.assettracker.domain.model.Chain
import com.crosschain.assettracker.domain.model.TransferStatus
import com.crosschain.assettracker.domain.model.statusToProgress
import com.crosschain.assettracker.ui.mvi.main.MainIntent
import com.crosschain.assettracker.ui.mvi.main.MainSideEffect
import com.crosschain.assettracker.ui.mvi.main.MainUiState
import com.crosschain.assettracker.ui.theme.LocalColorScheme
import com.reown.appkit.ui.components.internal.AppKitComponent
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigInteger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.setIntent(MainIntent.LoadAccountFromCache)
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is MainSideEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }

                is MainSideEffect.ReloadAccountAddressAndBalance -> {
                    viewModel.setIntent(MainIntent.LoadAccountFromWallet)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Cross-Chain Asset Tracker") })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            if (state.shouldConnectWallet) {
                val modalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

                ModalBottomSheet(
                    modifier = Modifier.fillMaxSize(),
                    sheetState = modalSheetState,
                    onDismissRequest = {
                        coroutineScope.launch { modalSheetState.hide() }
                    },
                    content = {
                        AppKitComponent(
                            shouldOpenChooseNetwork = true,
                            closeModal = {
                                coroutineScope.launch { modalSheetState.hide() }
                                viewModel.setIntent(MainIntent.LoadAccountFromWallet)
                            }
                        )
                    }
                )
            } else {
                val ethRebaseTokenBalanceInfo = state.ethRebaseTokenBalanceInfo
                val arbRebaseTokenBalanceInfo = state.arbRebaseTokenBalanceInfo

                LaunchedEffect(Unit) {
                    viewModel.setIntent(MainIntent.LoadBalance(Chain.ETHEREUM))
                    viewModel.setIntent(MainIntent.LoadBalance(Chain.ARBITRUM))
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (ethRebaseTokenBalanceInfo != null && arbRebaseTokenBalanceInfo != null) {
                        BalanceCard(ethRebaseTokenBalanceInfo)
                        HorizontalDivider(modifier = Modifier.height(24.dp), color = Color.Transparent)
                        BalanceCard(arbRebaseTokenBalanceInfo)
                        HorizontalDivider(modifier = Modifier.height(24.dp), color = Color.Transparent)
                        Timber.tag("MainScreen").d("state = $state")
                        CcipTrackingCard(state) { amountToSend ->
                            viewModel.setIntent(MainIntent.TransferTokens(BigInteger(amountToSend), Chain.ETHEREUM, Chain.ARBITRUM))
                        }
                    }
                }

                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

        }
    }

}

@Composable
fun BalanceCard(balanceInfo: BalanceInfo) {
    val amount = balanceInfo.amount
    val chain = balanceInfo.chain

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Real-Time Balance on ${chain.name}", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            if (amount.isNotBlank()) {
                Text(
                    text = "$amount ${balanceInfo.tokenSymbol}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Token Interest Rate: ${balanceInfo.baseInterestRate}",
                    fontSize = 14.sp,
                    color = LocalColorScheme.current.tertiary
                )
                Text(
                    text = "User Interest Rate: ${balanceInfo.currentInterestRate}",
                    fontSize = 14.sp,
                    color = LocalColorScheme.current.tertiary
                )
            }
        }
    }
}

@Composable
fun CcipTrackingCard(state: MainUiState, onSendClick: (String) -> Unit) {
    var amount by rememberSaveable { mutableStateOf("0") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("CCIP Transfer Tracking", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))

            if (state.ccipTransfer == null) {
                TextField(
                    value = amount,
                    onValueChange = { value ->
                        amount = value
                    },
                    label = { Text("Amount") }
                )
                Button(onClick = { onSendClick(amount) }) {
                    Text("Send cross chain tokens")
                }
            } else {
                Text("From: ${state.ccipTransfer.sourceChainName} -> To: ${state.ccipTransfer.destinationChainName}")
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                    Text(modifier = Modifier.weight(1f), text = "Status: ${state.ccipTransfer.status}")
                    if (state.ccipTransfer.status != TransferStatus.SUCCESS && state.ccipTransfer.status != TransferStatus.FAILED) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Bottom).size(20.dp), strokeWidth = 3.dp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { state.ccipTransfer.statusToProgress()},
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
                Text(
                    text = "${(state.ccipTransfer.statusToProgress() * 100).toInt()}%",
                    modifier = Modifier.align(Alignment.End),
                    fontSize = 12.sp
                )
            }
        }
    }
}