package com.crosschain.assettracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.crosschain.assettracker.data.local.database.dao.CcipSentRequestDao
import com.crosschain.assettracker.data.local.database.dao.RebaseTokenMetaDataDao
import com.crosschain.assettracker.data.local.database.entity.CcipSentRequestEntity
import com.crosschain.assettracker.data.local.database.entity.RebaseTokenMetadataEntity

@Database(entities = [RebaseTokenMetadataEntity::class, CcipSentRequestEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rebaseTokenMetaDataDao(): RebaseTokenMetaDataDao
    abstract fun ccipSentRequestDao(): CcipSentRequestDao
}