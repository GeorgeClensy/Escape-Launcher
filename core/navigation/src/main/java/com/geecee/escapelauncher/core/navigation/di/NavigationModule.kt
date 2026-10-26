package com.geecee.escapelauncher.core.navigation.di

import com.geecee.escapelauncher.core.navigation.NavRouter
import com.geecee.escapelauncher.core.navigation.NavRouterImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NavigationModule {

    @Binds
    @Singleton
    abstract fun bindNavRouter(impl: NavRouterImpl): NavRouter
}
