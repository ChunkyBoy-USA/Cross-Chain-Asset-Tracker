package com.crosschain.assettracker.data.network

import com.crosschain.assettracker.data.model.CcipTransferDetail
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import javax.inject.Singleton

@Singleton
interface CcipApiService {
    @GET("v2/messages/{messageId}")
    suspend fun getCcipTransferDetails(
        @Path("messageId") messageId: String
    ): Response<CcipTransferDetail>
}