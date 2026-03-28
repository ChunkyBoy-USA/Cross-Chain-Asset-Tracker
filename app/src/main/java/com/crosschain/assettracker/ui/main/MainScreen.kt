package com.crosschain.assettracker.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crosschain.assettracker.R
import com.crosschain.assettracker.domain.model.BalanceInfo
import com.crosschain.assettracker.domain.model.Chain
import com.crosschain.assettracker.ui.mvi.main.MainIntent
import com.reown.appkit.ui.AppKitTheme
import com.reown.appkit.ui.components.internal.AppKitComponent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.setIntent(MainIntent.LoadAccountFromCache)
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
            }else {
                val ethRebaseTokenBalanceInfo = state.ethRebaseTokenBalanceInfo
                val arbRebaseTokenBalanceInfo = state.arbRebaseTokenBalanceInfo

                LaunchedEffect(ethRebaseTokenBalanceInfo) {
                    if (ethRebaseTokenBalanceInfo == null) {
                        viewModel.setIntent(MainIntent.LoadBalance(Chain.ETHEREUM))
                    }
                }

                LaunchedEffect(arbRebaseTokenBalanceInfo) {
                    if (arbRebaseTokenBalanceInfo == null) {
                        viewModel.setIntent(MainIntent.LoadBalance(Chain.ARBITRUM))
                    }
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (ethRebaseTokenBalanceInfo != null && arbRebaseTokenBalanceInfo != null) {
                        BalanceCard(ethRebaseTokenBalanceInfo)
                        HorizontalDivider(modifier = Modifier.height(24.dp))
                        BalanceCard(arbRebaseTokenBalanceInfo)
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
                    text = "Base Interest Rate: ${balanceInfo.baseInterestRate}",
                    fontSize = 12.sp,
                    color = colorResource(R.color.purple_700)
                )
                Text(
                    text = "Current Interest Rate: ${balanceInfo.currentInterestRate}",
                    fontSize = 12.sp,
                    color = colorResource(R.color.purple_200)
                )
            }
        }
    }
}