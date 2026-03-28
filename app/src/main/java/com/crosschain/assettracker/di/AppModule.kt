package com.crosschain.assettracker.di

import com.crosschain.assettracker.data.repository.AccountRepositoryImpl
import com.crosschain.assettracker.data.repository.RebaseTokenRepositoryImpl
import com.crosschain.assettracker.data.repository.CcipRepositoryImpl
import com.crosschain.assettracker.domain.AccountRepository
import com.crosschain.assettracker.domain.RebaseTokenRepository
import com.crosschain.assettracker.domain.CcipRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAccountRepository(
        accountRepositoryImpl: AccountRepositoryImpl
    ): AccountRepository

    @Binds
    @Singleton
    abstract fun bindCcipRepository(
        ccipRepositoryImpl: CcipRepositoryImpl
    ): CcipRepository

    @Binds
    @Singleton
    abstract fun bindBalanceRepository(
        balanceRepositoryImpl: RebaseTokenRepositoryImpl
    ): RebaseTokenRepository
}
