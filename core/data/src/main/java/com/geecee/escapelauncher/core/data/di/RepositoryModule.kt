package com.geecee.escapelauncher.core.data.di

import com.geecee.escapelauncher.core.data.repository.AppsRepositoryImpl
import com.geecee.escapelauncher.core.data.repository.ModifiedAppsRepositoryImpl
import com.geecee.escapelauncher.core.data.repository.ScreenTimeRepositoryImpl
import com.geecee.escapelauncher.core.domain.repository.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.ScreenTimeRepository
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
}
