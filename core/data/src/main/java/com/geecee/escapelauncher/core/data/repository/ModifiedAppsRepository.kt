package com.geecee.escapelauncher.core.data.repository

import com.geecee.escapelauncher.core.data.entity.ModifiedAppEntity
import kotlinx.coroutines.flow.Flow

interface ModifiedAppsRepository {
    fun getHiddenPackageIdsFlow(): Flow<List<String>>

    fun getChallengePackageIdsFlow(): Flow<List<String>>

    fun getFavouriteAppsInOrderFlow(): Flow<List<ModifiedAppEntity>>
    suspend fun getByPackageId(packageId: String): ModifiedAppEntity?

    suspend fun setDisplayName(packageId: String, displayName: String?)

    suspend fun getDisplayName(packageId: String): String?

    suspend fun clearDisplayName(packageId: String)

    suspend fun setHidden(packageId: String, isHidden: Boolean)

    suspend fun isHidden(packageId: String): Boolean

    suspend fun setChallenge(packageId: String, isChallenge: Boolean)

    suspend fun isChallenge(packageId: String): Boolean

    suspend fun setFavouritePosition(packageId: String, favouritePosition: Double?)

    suspend fun getFavouritePosition(packageId: String): Double?

    suspend fun clearFavouritePosition(packageId: String)

    suspend fun isFavourite(packageId: String): Boolean

    suspend fun addFavourite(packageId: String)

    suspend fun removeFavourite(packageId: String)

    suspend fun reorderFavouriteApp(packageId: String, fromIndex: Int, toIndex: Int)

    suspend fun tidyFavouritePositions()

    suspend fun getFavouriteAppsInOrder(): List<ModifiedAppEntity>

    suspend fun getHiddenPackageIds(): List<String>

    suspend fun getChallengePackageIds(): List<String>

    suspend fun purgeAppsWithNoData(): Int

    suspend fun deleteByPackageId(packageId: String)
}