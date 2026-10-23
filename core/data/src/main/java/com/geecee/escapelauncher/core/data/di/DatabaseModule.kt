package com.geecee.escapelauncher.core.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.geecee.escapelauncher.core.data.database.AppDatabase
import com.geecee.escapelauncher.core.data.database.AppUsageDao
import com.geecee.escapelauncher.core.data.database.ModifiedAppsDao
import com.geecee.escapelauncher.core.data.database.ModifiedAppsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS app_usage_new (
                    packageName TEXT NOT NULL,
                    date TEXT NOT NULL,
                    totalTime INTEGER NOT NULL,
                    PRIMARY KEY(packageName, date)
                )
                """.trimIndent()
            )
            // Existing rows stored packageName as "$packageName-$date" (date is YYYY-MM-DD = 10 chars)
            db.execSQL(
                """
                INSERT INTO app_usage_new (packageName, date, totalTime)
                SELECT 
                    CASE 
                        WHEN LENGTH(packageName) > 11 AND INSTR(packageName, '-') > 0
                        THEN SUBSTR(packageName, 1, LENGTH(packageName) - 11)
                        ELSE packageName 
                    END,
                    CASE 
                        WHEN LENGTH(packageName) >= 10 AND INSTR(packageName, '-') > 0
                        THEN SUBSTR(packageName, -10)
                        ELSE '' 
                    END,
                    totalTime
                FROM app_usage
                """.trimIndent()
            )
            db.execSQL("DROP TABLE app_usage")
            db.execSQL("ALTER TABLE app_usage_new RENAME TO app_usage")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_app_usage_date ON app_usage(date)")
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_usage_database"
        )
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    fun provideAppUsageDao(database: AppDatabase): AppUsageDao {
        return database.appUsageDao()
    }

    @Provides
    @Singleton
    fun provideModifiedAppsDatabase(
        @ApplicationContext context: Context
    ): ModifiedAppsDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = ModifiedAppsDatabase::class.java,
            "modified_apps_database"
        ).build()
    }

    @Provides
    fun provideModifiedAppsDao(database: ModifiedAppsDatabase): ModifiedAppsDao {
        return database.modifiedAppsDao()
    }
}
