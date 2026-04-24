package com.geecee.escapelauncher.core.data.repository

import com.geecee.escapelauncher.core.data.database.ModifiedAppsDao
import com.geecee.escapelauncher.core.data.entity.ModifiedAppEntity
import kotlinx.coroutines.flow.Flow
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

    override fun getFavouriteAppsInOrderFlow(): Flow<List<ModifiedAppEntity>> =
        modifiedAppsDao.getFavouriteAppsInOrderFlow()

    override suspend fun getByPackageId(packageId: String): ModifiedAppEntity? {
        return modifiedAppsDao.getByPackageId(packageId)
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

    override suspend fun getFavouriteAppsInOrder(): List<ModifiedAppEntity> {
        return modifiedAppsDao.getFavouriteAppsInOrder()
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