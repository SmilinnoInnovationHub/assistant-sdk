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
            .setToken("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1bmlxdWVfbmFtZSI6ImhhbWhvdXNoQGdtYWlsLmNvbSIsImVtYWlsIjoiaGFtaG91c2hAZ21haWwuY29tIiwibmFtZWlkIjoiYTFjZDkyMTgtNDI4MS00Nzc1LWExZDMtMjA0OGM0NDE4YmMxIiwicm9sZSI6IlVTRVIiLCJuYmYiOjE3MDI4ODY2ODEsImV4cCI6MTcwMzQ5MTQ4MSwiaWF0IjoxNzAyODg2NjgxLCJpc3MiOiJlbnRlcnByaXNlLWlkZW50aXR5IiwiYXVkIjoiQWRtaW5Qb3J0YWwifQ.pXnQbQVhsrkYy6eO1ZQ1Zfi1pS_UIxtHYBHJ4F98rj0")
            .setPublisher("sdk")
            .setClientId("")
            .isTtsEnabled(true)
            .setRetryConnectionTime(1000)
            .build()
    }


}
