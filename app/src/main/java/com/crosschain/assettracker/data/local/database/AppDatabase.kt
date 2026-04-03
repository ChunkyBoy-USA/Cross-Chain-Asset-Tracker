package com.crosschain.assettracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.crosschain.assettracker.data.local.database.dao.CcipSentRequestDao
import com.crosschain.assettracker.data.local.database.dao.RebaseTokenMetaDataDao
import com.crosschain.assettracker.data.local.database.dao.RouterAllowanceDao
import com.crosschain.assettracker.data.local.database.entity.CcipSentRequestEntity
import com.crosschain.assettracker.data.local.database.entity.PendingRouterAllowanceEntity
import com.crosschain.assettracker.data.local.database.entity.RebaseTokenMetadataEntity
import com.crosschain.assettracker.data.local.database.entity.RouterAllowanceEntity

@Database(entities = [RebaseTokenMetadataEntity::class, CcipSentRequestEntity::class, PendingRouterAllowanceEntity::class, RouterAllowanceEntity::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rebaseTokenMetaDataDao(): RebaseTokenMetaDataDao
    abstract fun ccipSentRequestDao(): CcipSentRequestDao
    abstract fun routerAllowanceDao(): RouterAllowanceDao
}