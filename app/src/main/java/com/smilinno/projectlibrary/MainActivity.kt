package com.smilinno.projectlibrary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.smilinno.projectlibrary.databinding.ActivityMainBinding
import com.smilinno.smilinnolibrary.SmilinnoLibrary

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val library = SmilinnoLibrary.Builder()
            .setContext(this)
            .setToken("token753678373785")
            .build()

        library.context
        library.token
        library.connected()
        Log.d("TAG", "${library.connected()}")

    }
}