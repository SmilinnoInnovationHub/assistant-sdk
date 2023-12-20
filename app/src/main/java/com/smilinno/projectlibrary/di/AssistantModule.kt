package com.smilinno.projectlibrary.di

import android.content.Context
import com.smilinno.smilinnolibrary.AssistantLibrary
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AssistantModule {


    @Singleton
    @Provides
    fun provideAssistantSDK(@ApplicationContext context: Context): AssistantLibrary {
        return AssistantLibrary.Builder(context)
            .setToken("API_TOKEN")
            .setPublisher("sdk")
            .setClientId("")
            .isTtsEnabled(true)
            .setRetryConnectionTime(1000)
            .build()
    }


}
