@file:Suppress("KotlinConstantConditions") // Stop Android Studio saying that isFoss is always false

package com.geecee.escapelauncher

import com.geecee.escapelauncher.core.common.AppConfiguration
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

class AppConfigurationImpl @Inject constructor() : AppConfiguration {
    override val isFoss: Boolean = BuildConfig.IS_FOSS
    override val appVersion: String = BuildConfig.APP_VERSION
    override val appName: String = BuildConfig.APP_NAME
    override val appFlavour: String = BuildConfig.APP_FLAVOUR
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindAppConfiguration(
        impl: AppConfigurationImpl
    ): AppConfiguration
}
