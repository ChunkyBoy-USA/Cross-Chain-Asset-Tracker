package com.crosschain.assettracker.di

import android.content.Context
import androidx.room.Room
import com.crosschain.assettracker.data.local.database.AppDatabase
import com.crosschain.assettracker.data.local.database.dao.CcipSentRequestDao
import com.crosschain.assettracker.data.local.database.dao.RebaseTokenMetaDataDao
import com.crosschain.assettracker.data.local.database.dao.RouterAllowanceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "asset_tracker_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideRebaseTokenMetadataDao(db: AppDatabase): RebaseTokenMetaDataDao =
        db.rebaseTokenMetaDataDao()

    @Provides
    fun provideCcipSentRequestDao(db: AppDatabase): CcipSentRequestDao = db.ccipSentRequestDao()
    @Provides
    fun provideRouterAllowanceDao(db: AppDatabase): RouterAllowanceDao = db.routerAllowanceDao()
}