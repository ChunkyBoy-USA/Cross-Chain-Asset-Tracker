package com.crosschain.assettracker.ui.mvi.main

sealed class MainSideEffect {
    data class ShowToast(val message: String) : MainSideEffect()
}