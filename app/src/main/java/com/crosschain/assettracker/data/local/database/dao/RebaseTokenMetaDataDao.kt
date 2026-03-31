package com.crosschain.assettracker.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.crosschain.assettracker.data.local.database.entity.RebaseTokenMetadataEntity

@Dao
interface RebaseTokenMetaDataDao {
    @Query("SELECT * FROM rebase_token_metadata WHERE chainId = :chainId AND tokenAddress = :address LIMIT 1")
    suspend fun getMetadata(chainId: String, address: String): RebaseTokenMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: RebaseTokenMetadataEntity)
}