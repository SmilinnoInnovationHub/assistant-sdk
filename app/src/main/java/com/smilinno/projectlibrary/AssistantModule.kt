package com.smilinno.projectlibrary

import android.content.Context
import com.smilinno.smilinnolibrary.SmilinnoLibrary
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
    fun provideAssistantSDK(@ApplicationContext context: Context): SmilinnoLibrary {
        return SmilinnoLibrary.Builder(context)
//        "caccc7b8-c10c-41f2-9c6f-189c75decc38"
//        eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiI0MjliMzhjNC1kZmIyLTQ0MmQtOThiMi1iNjdhNTU3NzI1NjkiLCJuYmYiOjE2OTY2ODYzMzAsImV4cCI6MTY5ODQxNDMzMCwiaWF0IjoxNjk2Njg2MzMwLCJpc3MiOiJaaXR1cmUiLCJhdWQiOiJaaXR1cmUifQ.dToO00cq1D6RHQZ-bZrJjVW0-wrye8wjRIR3pQob0xE
            .setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiI0MjliMzhjNC1kZmIyLTQ0MmQtOThiMi1iNjdhNTU3NzI1NjkiLCJuYmYiOjE2OTY2ODYzMzAsImV4cCI6MTY5ODQxNDMzMCwiaWF0IjoxNjk2Njg2MzMwLCJpc3MiOiJaaXR1cmUiLCJhdWQiOiJaaXR1cmUifQ.dToO00cq1D6RHQZ-bZrJjVW0-wrye8wjRIR3pQob0xE")
            .setPublisher("sdk")
            .setClientId("")
            .build()
    }


}
