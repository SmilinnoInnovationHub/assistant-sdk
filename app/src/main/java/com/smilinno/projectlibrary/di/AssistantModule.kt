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
            .setToken("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1bmlxdWVfbmFtZSI6InRlc3RAZ21haWwuY29tIiwiZW1haWwiOiJ0ZXN0QGdtYWlsLmNvbSIsIm5hbWVpZCI6IjMyMzdiZGRjLWYxMTYtNDkwMi1iM2UyLTVhODYzZjU5M2MwZiIsInJvbGUiOiJVU0VSIiwibmJmIjoxNzAxMjU3MDAwLCJleHAiOjE3MDE4NjE4MDAsImlhdCI6MTcwMTI1NzAwMCwiaXNzIjoiZW50ZXJwcmlzZS1pZGVudGl0eSIsImF1ZCI6IkFkbWluUG9ydGFsIn0.5-V3Kg-__mG6l4VAsxy5bsDAkh7Pf-aXM0VrXWyBcmM")
            .setPublisher("sdk")
            .setClientId("")
            .isTtsEnabled(true)
            .setRetryConnectionTime(1000)
            .build()
    }


}
