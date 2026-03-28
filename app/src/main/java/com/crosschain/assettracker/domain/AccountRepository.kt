package com.crosschain.assettracker.domain

interface AccountRepository {
    fun loadAccountFromAppKit(): Boolean
    fun getCurrentAccountAddress(): String?
}
