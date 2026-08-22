@file:Suppress("unused")

package com.geecee.escapelauncher.feature.weather.di

import com.geecee.escapelauncher.feature.weather.WeatherImpl
import com.geecee.escapelauncher.feature.weather.WeatherProxy
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WeatherModule {
    @Binds
    @Singleton
    abstract fun WeatherProxy(
        weatherImpl: WeatherImpl
    ): WeatherProxy
}