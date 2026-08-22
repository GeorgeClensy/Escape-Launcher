@file:Suppress("unused")

package com.geecee.escapelauncher.core.cloudmessaging.di

import com.geecee.escapelauncher.core.cloudmessaging.MessagingInitializer
import com.geecee.escapelauncher.core.cloudmessaging.MessagingInitializerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CloudMessagingModule {
    @Binds
    @Singleton
    abstract fun bindMessagingInitializer(
        impl: MessagingInitializerImpl
    ): MessagingInitializer
}