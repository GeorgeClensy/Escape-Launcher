package com.geecee.escapelauncher.core.data.di

import com.geecee.escapelauncher.core.data.repository.ScreenTimeRepository
import com.geecee.escapelauncher.core.data.repository.ScreenTimeRepositoryImpl
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
}
