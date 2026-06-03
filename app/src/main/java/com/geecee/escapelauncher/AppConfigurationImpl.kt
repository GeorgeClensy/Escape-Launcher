@file:Suppress("KotlinConstantConditions") // Stop android studio saying that

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
