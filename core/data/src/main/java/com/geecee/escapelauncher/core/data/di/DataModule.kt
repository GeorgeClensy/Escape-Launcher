package com.geecee.escapelauncher.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.geecee.escapelauncher.core.data.datastore.settingsDataStore
import com.geecee.escapelauncher.core.data.repository.settings.*
import com.geecee.escapelauncher.core.domain.repository.settings.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused") // It is used by the generated build code.
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindAppearanceRepository(
        impl: AppearanceRepositoryImpl
    ): AppearanceRepository

    @Binds
    @Singleton
    abstract fun bindClockRepository(
        impl: ClockRepositoryImpl
    ): ClockRepository

    @Binds
    @Singleton
    abstract fun bindLauncherBehaviorRepository(
        impl: LauncherBehaviorRepositoryImpl
    ): LauncherBehaviorRepository

    @Binds
    @Singleton
    abstract fun bindSearchSettingsRepository(
        impl: SearchSettingsRepositoryImpl
    ): SearchSettingsRepository

    @Binds
    @Singleton
    abstract fun bindWidgetSettingsRepository(
        impl: WidgetSettingsRepositoryImpl
    ): WidgetSettingsRepository

    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(
        impl: OnboardingRepositoryImpl
    ): OnboardingRepository

    @Binds
    @Singleton
    abstract fun bindScreenTimeSettingsRepository(
        impl: ScreenTimeSettingsRepositoryImpl
    ): ScreenTimeSettingsRepository

    @Binds
    @Singleton
    abstract fun bindWeatherSettingsRepository(
        impl: WeatherSettingsRepositoryImpl
    ): WeatherSettingsRepository

    companion object {
        @Provides
        @Singleton
        fun provideDataStore(
            @ApplicationContext context: Context
        ): DataStore<Preferences> = context.settingsDataStore
    }
}