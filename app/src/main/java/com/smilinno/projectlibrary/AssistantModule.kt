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
            .setToken("Bearer eyJhbGciOiJBMTI4S1ciLCJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwidHlwIjoiSldUIiwiY3R5IjoiSldUIn0.z0f3lVPIOi2Fj2Y6_2iLObGBb4iVWInIK4Fl7dLuZr667V4kTfl2-w.qnLfPZo8GM7rwrl29209-g.p2ZiBhmrn0gpEFs_oXa-JAX4Zig56gmLzyfkTLxhuPw04ENR_e4pARtwF0AoubnsBzWpE0JPnhZSiXN0KIqQOCqyFYliP8JLdUvcatF4vc_KllDWdqZdOG9XEYbClxl77l0L4CCYVtMKgMS8PoUVQ4ll7WKD_usYL-5rzLVo35i92FnJyQ_QkYegNsY46iuqs43nvVWXvdVfVF5rnSdgw4ubiTWD4c5okV9IBWkdAwzDYQzIoFWCfrR3exKl3V9nkSHy3b5MVr-Jx706gDcDfOh-lkiu8d05lwpIkQpn2yJdxC421tovtdKPGDR8MfyqQjcFkRwMvvrVNzw-ttdc6mqVkozZJjubhuccijcgzFs.SddB6bvVNMosgnOZ5CpjPg")
            .setPublisher("sdk")
            .setClientId("")
            .setRetryConnectionTime(1000)
            .build()
    }


}
