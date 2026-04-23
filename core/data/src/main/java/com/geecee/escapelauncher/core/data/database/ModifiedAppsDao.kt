package com.geecee.escapelauncher.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.geecee.escapelauncher.core.data.entity.ModifiedAppEntity

@Dao
interface ModifiedAppsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(app: ModifiedAppEntity): Long

    @Update
    suspend fun update(app: ModifiedAppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(app: ModifiedAppEntity)

    @Query("SELECT * FROM modifiedApps WHERE packageName = :packageName LIMIT 1")
    suspend fun getBypackageName(packageName: String): ModifiedAppEntity?

    @Query("SELECT displayName FROM modifiedApps WHERE packageName = :packageName LIMIT 1")
    suspend fun getDisplayName(packageName: String): String?

    @Transaction
    suspend fun setDisplayName(packageName: String, displayName: String?) {
        val current = getBypackageName(packageName)
        if (current == null) {
            upsert(
                ModifiedAppEntity(
                    packageName = packageName,
                    displayName = displayName,
                    isHidden = false,
                    isChallenge = false,
                    favouritePosition = null
                )
            )
        } else {
            upsert(current.copy(displayName = displayName))
        }
    }

    @Transaction
    suspend fun clearDisplayName(packageName: String) {
        setDisplayName(packageName, null)
    }

    @Query("UPDATE modifiedApps SET isHidden = :isHidden WHERE packageName = :packageName")
    suspend fun updateIsHidden(packageName: String, isHidden: Boolean)

    @Query("SELECT isHidden FROM modifiedApps WHERE packageName = :packageName LIMIT 1")
    suspend fun getIsHidden(packageName: String): Boolean?

    @Query("UPDATE modifiedApps SET isChallenge = :isChallenge WHERE packageName = :packageName")
    suspend fun updateIsChallenge(packageName: String, isChallenge: Boolean)

    @Query("SELECT isChallenge FROM modifiedApps WHERE packageName = :packageName LIMIT 1")
    suspend fun getIsChallenge(packageName: String): Boolean?

    @Query("UPDATE modifiedApps SET favouritePosition = :favouritePosition WHERE packageName = :packageName")
    suspend fun updateFavouritePosition(packageName: String, favouritePosition: Double?)

    @Query("SELECT favouritePosition FROM modifiedApps WHERE packageName = :packageName LIMIT 1")
    suspend fun getFavouritePosition(packageName: String): Double?

    @Transaction
    suspend fun setIsHidden(packageName: String, isHidden: Boolean) {
        ensureRowExists(packageName)
        updateIsHidden(packageName, isHidden)
    }

    @Transaction
    suspend fun setIsChallenge(packageName: String, isChallenge: Boolean) {
        ensureRowExists(packageName)
        updateIsChallenge(packageName, isChallenge)
    }

    @Transaction
    suspend fun setFavouritePosition(packageName: String, favouritePosition: Double?) {
        ensureRowExists(packageName)
        updateFavouritePosition(packageName, favouritePosition)
    }

    @Transaction
    suspend fun clearFavouritePosition(packageName: String) {
        setFavouritePosition(packageName, null)
    }

    @Transaction
    suspend fun ensureRowExists(packageName: String) {
        insertIfMissing(
            ModifiedAppEntity(
                packageName = packageName,
                displayName = null,
                isHidden = false,
                isChallenge = false,
                favouritePosition = null
            )
        )
    }

    @Query(
        """
        SELECT *
        FROM modifiedApps
        WHERE favouritePosition IS NOT NULL
        ORDER BY favouritePosition ASC,
                 COALESCE(displayName, packageName) COLLATE NOCASE ASC
        """
    )
    suspend fun getFavouriteAppsInOrder(): List<ModifiedAppEntity>

    @Query(
        """
        SELECT packageName
        FROM modifiedApps
        WHERE isHidden = 1
        ORDER BY COALESCE(displayName, packageName) COLLATE NOCASE ASC
        """
    )
    suspend fun getHiddenpackageNames(): List<String>

    @Query(
        """
        SELECT packageName
        FROM modifiedApps
        WHERE isChallenge = 1
        ORDER BY COALESCE(displayName, packageName) COLLATE NOCASE ASC
        """
    )
    suspend fun getChallengepackageNames(): List<String>

    @Query("SELECT EXISTS(SELECT 1 FROM modifiedApps WHERE packageName = :packageName AND isHidden = 1)")
    suspend fun isHidden(packageName: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM modifiedApps WHERE packageName = :packageName AND isChallenge = 1)")
    suspend fun isChallenge(packageName: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM modifiedApps WHERE packageName = :packageName AND favouritePosition IS NOT NULL)")
    suspend fun isFavourite(packageName: String): Boolean

    @Query("DELETE FROM modifiedApps WHERE packageName = :packageName")
    suspend fun deleteBypackageName(packageName: String)

    @Query(
        """
        DELETE FROM modifiedApps
        WHERE displayName IS NULL
          AND isHidden = 0
          AND isChallenge = 0
          AND favouritePosition IS NULL
        """
    )
    suspend fun purgeAppsWithNoData(): Int
}