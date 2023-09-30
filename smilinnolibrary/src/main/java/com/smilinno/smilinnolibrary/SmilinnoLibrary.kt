package com.smilinno.smilinnolibrary

import android.content.Context

class SmilinnoLibrary private constructor(builder: Builder) {
     var token: String? = null
     var context: Context

     fun connected() : String? {
         return token
     }

    class Builder {

        private lateinit var context: Context
        private var token: String? = null

        fun setContext(context: Context) = apply { this.context = context }
        fun setToken(token: String) = apply { this.token = token }

        fun build() = SmilinnoLibrary(this)
        fun getContext() = context
        fun getToken() = token
    }

    init {
        token = builder.getToken()
        context = builder.getContext()
    }

}