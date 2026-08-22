@file:Suppress("unused")

package com.geecee.escapelauncher.core.analytics.di

import com.geecee.escapelauncher.core.analytics.AnalyticsProxy
import com.geecee.escapelauncher.core.analytics.AnalyticsProxyImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {
    @Binds
    @Singleton
    abstract fun bindAnalyticsProxy(
        analyticsProxyImpl: AnalyticsProxyImpl
    ): AnalyticsProxy
}
