package com.geecee.escapelauncher.core.data.di

import com.geecee.escapelauncher.core.data.repository.android.AppsRepositoryImpl
import com.geecee.escapelauncher.core.data.repository.android.ManagedProfileRepositoryImpl
import com.geecee.escapelauncher.core.data.repository.db.ModifiedAppsRepositoryImpl
import com.geecee.escapelauncher.core.data.repository.db.ScreenTimeRepositoryImpl
import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.android.ManagedProfileRepository
import com.geecee.escapelauncher.core.domain.repository.db.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.db.ScreenTimeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused") // It is used by the generated build code.
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindScreenTimeRepository(
        screenTimeRepositoryImpl: ScreenTimeRepositoryImpl
    ): ScreenTimeRepository

    @Binds
    @Singleton
    abstract fun bindModifiedAppsRepository(
        modifiedAppsRepositoryImpl: ModifiedAppsRepositoryImpl
    ): ModifiedAppsRepository

    @Binds
    @Singleton
    abstract fun bindAppsRepository(
        appsRepositoryImpl: AppsRepositoryImpl
    ): AppsRepository

    @Binds
    @Singleton
    abstract fun bindManagedProfileRepository(
        managedProfileRepositoryImpl: ManagedProfileRepositoryImpl
    ): ManagedProfileRepository
}
