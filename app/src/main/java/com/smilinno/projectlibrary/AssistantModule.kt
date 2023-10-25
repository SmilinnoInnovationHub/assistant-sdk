package com.smilinno.projectlibrary

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
            .setToken("caccc7b8-c10c-41f2-9c6f-189c75decc38")
            .isTtsEnabled(true)
            .setPublisher("sdk")
            .setClientId("")
            .setRetryConnectionTime(1000)
            .build()
    }


}
