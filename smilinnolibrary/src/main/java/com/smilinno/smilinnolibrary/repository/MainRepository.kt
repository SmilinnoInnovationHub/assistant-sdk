package com.smilinno.smilinnolibrary.repository

import android.content.Context
import com.smilinno.smilinnolibrary.model.Link
import com.smilinno.smilinnolibrary.model.Setting
import com.smilinno.smilinnolibrary.model.UserSetting
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal object MainRepository {
    var links: HashMap<String, Link> = hashMapOf()
    var setting: Setting? = null
    var userSetting : UserSetting? = null
    var serverTimeDiff: Long = 0
    var featureFlags : HashMap<String, Boolean> = hashMapOf()

}