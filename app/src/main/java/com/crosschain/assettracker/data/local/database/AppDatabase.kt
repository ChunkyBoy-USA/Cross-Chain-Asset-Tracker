package com.crosschain.assettracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.crosschain.assettracker.data.local.database.dao.RebaseTokenMetaDataDao
import com.crosschain.assettracker.data.local.database.entities.RebaseTokenMetadataEntity

@Database(entities = [RebaseTokenMetadataEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rebaseTokenMetaDataDao(): RebaseTokenMetaDataDao
}