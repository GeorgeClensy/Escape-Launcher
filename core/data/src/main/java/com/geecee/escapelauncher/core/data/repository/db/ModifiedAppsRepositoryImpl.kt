package com.geecee.escapelauncher.core.data.repository.db

import com.geecee.escapelauncher.core.data.database.ModifiedAppsDao
import com.geecee.escapelauncher.core.data.entity.ModifiedAppEntity
import com.geecee.escapelauncher.core.domain.repository.db.ModifiedAppsRepository
import com.geecee.escapelauncher.core.model.ModifiedApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModifiedAppsRepositoryImpl @Inject constructor(
    private val modifiedAppsDao: ModifiedAppsDao
) : ModifiedAppsRepository {
    override fun getHiddenPackageIdsFlow(): Flow<List<String>> =
        modifiedAppsDao.getHiddenPackageIdsFlow()

    override fun getChallengePackageIdsFlow(): Flow<List<String>> =
        modifiedAppsDao.getChallengePackageIdsFlow()

    override fun getFavouriteAppsInOrderFlow(): Flow<List<ModifiedApp>> =
        modifiedAppsDao.getFavouriteAppsInOrderFlow().map { entities ->
            entities.map { it.asExternalModel() }
        }

    override suspend fun getByPackageId(packageId: String): ModifiedApp? {
        return modifiedAppsDao.getByPackageId(packageId)?.asExternalModel()
    }

    override suspend fun setDisplayName(packageId: String, displayName: String?) {
        modifiedAppsDao.setDisplayName(packageId, displayName)
    }

    override suspend fun getDisplayName(packageId: String): String? {
        return modifiedAppsDao.getDisplayName(packageId)
    }

    override suspend fun clearDisplayName(packageId: String) {
        modifiedAppsDao.clearDisplayName(packageId)
    }

    override suspend fun setHidden(packageId: String, isHidden: Boolean) {
        modifiedAppsDao.setIsHidden(packageId, isHidden)
    }

    override suspend fun isHidden(packageId: String): Boolean {
        return modifiedAppsDao.isHidden(packageId)
    }

    override suspend fun setChallenge(packageId: String, isChallenge: Boolean) {
        modifiedAppsDao.setIsChallenge(packageId, isChallenge)
    }

    override suspend fun isChallenge(packageId: String): Boolean {
        return modifiedAppsDao.isChallenge(packageId)
    }

    override suspend fun setFavouritePosition(packageId: String, favouritePosition: Double?) {
        modifiedAppsDao.setFavouritePosition(packageId, favouritePosition)
    }

    override suspend fun getFavouritePosition(packageId: String): Double? {
        return modifiedAppsDao.getFavouritePosition(packageId)
    }

    override suspend fun clearFavouritePosition(packageId: String) {
        modifiedAppsDao.clearFavouritePosition(packageId)
    }

    override suspend fun isFavourite(packageId: String): Boolean {
        return modifiedAppsDao.isFavourite(packageId)
    }

    override suspend fun addFavourite(packageId: String) {
        val lastPos = modifiedAppsDao.getFavouriteAppsInOrder().lastOrNull()?.favouritePosition ?: -1.0
        modifiedAppsDao.setFavouritePosition(packageId, lastPos + 1.0)
    }

    override suspend fun removeFavourite(packageId: String) {
        modifiedAppsDao.clearFavouritePosition(packageId)
    }

    override suspend fun reorderFavouriteApp(packageId: String, fromIndex: Int, toIndex: Int) {
        val favorites = modifiedAppsDao.getFavouriteAppsInOrder()
        if (fromIndex !in favorites.indices || toIndex !in favorites.indices) return // Ensure indices are within bounds to avoid IndexOutOfBoundsException if the list changed concurrently
        if (fromIndex == toIndex) return

        val otherFavorites = favorites.filter { it.packageId != packageId }

        val newPosition: Double = when {
            toIndex == 0 -> {
                (otherFavorites.firstOrNull()?.favouritePosition ?: 0.0) - 1.0
            }
            toIndex >= otherFavorites.size -> {
                (otherFavorites.lastOrNull()?.favouritePosition ?: 0.0) + 1.0
            }
            else -> {
                val prevPos = otherFavorites[toIndex - 1].favouritePosition ?: 0.0
                val nextPos = otherFavorites[toIndex].favouritePosition ?: 0.0

                val gap = nextPos - prevPos
                if (gap < 1e-10) {
                    // If the gap is too small, we should tidy first and then recalculate
                    tidyFavouritePositions()
                    val freshFavorites = modifiedAppsDao.getFavouriteAppsInOrder()
                    val freshOther = freshFavorites.filter { it.packageId != packageId }
                    val freshPrev = freshOther[toIndex - 1].favouritePosition ?: 0.0
                    val freshNext = freshOther[toIndex].favouritePosition ?: 0.0
                    (freshPrev + freshNext) / 2.0
                } else {
                    prevPos + (gap / 2.0)
                }
            }
        }

        modifiedAppsDao.setFavouritePosition(packageId, newPosition)
    }

    override suspend fun tidyFavouritePositions() {
        val favorites = modifiedAppsDao.getFavouriteAppsInOrder()
        val tidied = favorites.mapIndexed { index, app ->
            app.copy(favouritePosition = index.toDouble())
        }
        modifiedAppsDao.upsertAll(tidied)
    }

    override suspend fun getFavouriteAppsInOrder(): List<ModifiedApp> {
        return modifiedAppsDao.getFavouriteAppsInOrder().map { it.asExternalModel() }
    }

    override suspend fun getHiddenPackageIds(): List<String> {
        return modifiedAppsDao.getHiddenPackageIds()
    }

    override suspend fun getChallengePackageIds(): List<String> {
        return modifiedAppsDao.getChallengePackageIds()
    }

    override suspend fun purgeAppsWithNoData(): Int {
        return modifiedAppsDao.purgeAppsWithNoData()
    }

    override suspend fun deleteByPackageId(packageId: String) {
        modifiedAppsDao.deleteByPackageId(packageId)
    }
}

fun ModifiedAppEntity.asExternalModel() = ModifiedApp(
    packageId = packageId,
    displayName = displayName,
    isHidden = isHidden,
    isChallenge = isChallenge,
    favouritePosition = favouritePosition
)