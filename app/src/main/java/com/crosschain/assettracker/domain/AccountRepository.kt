package com.crosschain.assettracker.domain

interface AccountRepository {
    fun loadAccounts(): Boolean
}
