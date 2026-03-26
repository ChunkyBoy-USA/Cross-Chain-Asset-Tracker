package com.crosschain.assettracker.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reown.appkit.ui.components.internal.AppKitComponent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    val modalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val coroutineScope = rememberCoroutineScope()
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }


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
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    modifier = Modifier.wrapContentSize(),
                    onClick = {
                        openBottomSheet = true
                    },
                    enabled = !state.isLoading,
                    content = { Text("Connect to Wallet") }
                )
            }


            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if (openBottomSheet) {
                ModalBottomSheet(
                    modifier = Modifier.fillMaxSize(),
                    sheetState = modalSheetState,
                    onDismissRequest = {
                        openBottomSheet = false
                    },
                    content = {
                        AppKitComponent(
                            shouldOpenChooseNetwork = true,
                            closeModal = { coroutineScope.launch { modalSheetState.hide() }}
                        )
                    }
                )
            }

        }

    }

}