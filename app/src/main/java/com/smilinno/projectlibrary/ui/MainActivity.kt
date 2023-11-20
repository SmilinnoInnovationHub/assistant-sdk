package com.smilinno.projectlibrary.ui

import PermissionManager
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.SpeechRecognizer
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.microsoft.signalr.HubConnectionState
import com.smilinno.projectlibrary.adapter.ChatAdapter
import com.smilinno.projectlibrary.databinding.ActivityMainNewBinding
import com.smilinno.projectlibrary.model.Chat
import com.smilinno.projectlibrary.util.PlayerApp
import com.smilinno.projectlibrary.util.hideKeyboard
import com.smilinno.projectlibrary.util.showSnackBar
import com.smilinno.smilinnolibrary.AssistantLibrary
import com.smilinno.smilinnolibrary.callback.AssistantListener
import com.smilinno.smilinnolibrary.callback.VoiceToTextListener
import com.smilinno.smilinnolibrary.model.MessageResponse
import com.smilinno.smilinnolibrary.model.MessageType
import com.smilinno.smilinnolibrary.model.MessageVoiceToText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val TAG: String = MainActivity::class.java.name
    private var mRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private var timerHasFinished = true
    private var startRecordTime = 0L
    private val recordDelayTime = 800
    private lateinit var binding: ActivityMainNewBinding
    @Inject lateinit var assistantLibrary: AssistantLibrary
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var permissionManager: PermissionManager
    private var voice: String? = null
    private var isRecognizerActivate = false
    private var recognizerIntent: Intent? = null
    private var speech: SpeechRecognizer? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainNewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        permissionManager = PermissionManager(this)
        bindVisibility()
        bindSend()
        bindAssistant()
        bindRecyclerView()
        showCallBack()
    }


    private fun bindSend() = with(binding.sendChat) {
        setOnClickListener {
            if (assistantLibrary.isConnected()){
                if (binding.chatEditText.text?.trim()?.isNotEmpty() == true) {
                    val convertToChat = Chat(
                        text = binding.chatEditText.text.toString(),
                        isAssistant = false,
                        custom = null,
                        type = MessageType.ASSISTANT
                    )
                    chatAdapter.addChat(convertToChat)
                    if (chatAdapter.itemCount > 0) {
                        binding.listChat.smoothScrollToPosition(0)
                    }
                    sendChatClick(binding.chatEditText.text.toString())
                    showIsTyping(true)
                }
            } else {
                binding.root.showSnackBar("لطفا پس از اتصال مجدد تلاش کنید")
            }
        }
    }

    private fun sendChatClick(text : String) {
        PlayerApp.releasePlayer()
        speech?.stopListening()
        if (text.trim().isNotEmpty()) {
            binding.root.hideKeyboard()
            assistantLibrary.sendTextToAssistant(text)
            binding.chatEditText.text?.clear()
            isRecognizerActivate = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ClickableViewAccessibility")
    private fun bindAssistant() = with(binding.sendVoice) {
        setOnClickListener {
            if (assistantLibrary.isConnected()) {
                binding.chatEditText.setText("")
                if (permissionManager.checkRecordAudioPermissionRequest(this@MainActivity)) {
                    recorderAssistant()
                }
            } else {
                binding.root.showSnackBar("لطفا پس از اتصال مجدد تلاش کنید")
            }

        }
    }

    private fun bindVisibility() = with(binding.chatEditText) {
        doAfterTextChanged {
            it?.let {
                if (it.isEmpty()) {
                    binding.sendChat.visibility = GONE
                    binding.sendVoice.visibility = VISIBLE
                } else {
                    binding.sendVoice.visibility = GONE
                    binding.sendChat.visibility = VISIBLE
                }
            }
        }
    }

    private fun bindRecyclerView() = with(binding.listChat) {
        layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, true)
        chatAdapter = ChatAdapter(mutableListOf())
        adapter = chatAdapter
    }

    private fun showCallBack() {
        assistantLibrary.setAssistantCallBack(object : AssistantListener {
            override fun onMessageReceive(message: MessageResponse) {
                lifecycleScope.launch(Dispatchers.Main) {
                    voice = message.voice
                    when (message.type) {

                        MessageType.ASSISTANT -> {
                            val convertToChat = Chat(
                                text = message.text,
                                voice = message.voice,
                                isAssistant = true,
                                type = MessageType.ASSISTANT,
                                custom = message.custom.toString()
                            )
                        chatAdapter.addChat(convertToChat)
                            if (chatAdapter.itemCount > 0) {
                                binding.listChat.smoothScrollToPosition(0)
                            }
                            showIsTyping(false)
                        }
                        MessageType.ERROR -> {
                            showIsTyping(false)
                            binding.root.showSnackBar(message.text.toString())
                        }
                        else -> {
                            showIsTyping(false)
                        }
                    }
                }
            }

            override fun onMessageError(e: Exception) {
                showIsTyping(false)
                binding.root.showSnackBar(e.toString())
            }

            override fun onConnectionStateChange(connectionState: HubConnectionState) {
                lifecycleScope.launch(Dispatchers.Main) {
                    binding.state.text = connectionState.name
                }
            }

        })
    }

    private fun recorderAssistant() {
        if (timerHasFinished) {
            releaseRecorder()
            cTimer?.start()
            timerHasFinished = false
            initMediaRecorder()
            startRecordTime = System.currentTimeMillis()
            binding.micAnimationContainer.visibility = View.VISIBLE
        } else {
            stopMic()
        }
    }

    private fun releaseRecorder() {
        try {
            if (mRecorder != null) {
                mRecorder?.stop()
                mRecorder?.reset()
                mRecorder?.release()
                mRecorder = null
            }
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "releaseRecorder: ", e)
        }
    }

    private var cTimer: CountDownTimer? = object :
        CountDownTimer(11000, 1000) {
        override fun onTick(millisUntilFinished: Long) {

        }

        override fun onFinish() {
            timerHasFinished = true
            binding.micAnimationContainer.visibility = View.INVISIBLE
            stopMic()
        }
    }

    private fun initMediaRecorder() {
        audioFilePath = this
            .getExternalFilesDir(null)?.absolutePath + "/" + "zich.3gp"
        try {
            mRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                MediaRecorder()
            }
            mRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mRecorder?.setAudioSamplingRate(16000)
            mRecorder?.setOutputFile(audioFilePath)
            mRecorder?.prepare()
            mRecorder?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopMic() {
        if (permissionManager.isRecordAudioPermissionGranted()) {
            binding.micAnimationContainer.visibility = View.INVISIBLE
            if (System.currentTimeMillis() < startRecordTime + recordDelayTime) {
                lifecycleScope.launch {
                    cTimer?.cancel()
                    timerHasFinished = true
                    delay(300)
                    releaseRecorder()
                }
            } else {
                releaseRecorder()
                cTimer?.cancel()
                timerHasFinished = true
                var audioBase64: String? = null
                if (audioFilePath != null) {
                    audioBase64 = getAudioBase64()
                }
                if (audioBase64 != null) {
                    assistantLibrary.sendVoiceToGetText(audioBase64,object : VoiceToTextListener {
                        override fun onTextReceive(message: MessageVoiceToText) {
                            lifecycleScope.launch(Dispatchers.Main) {
                                binding.chatEditText.setText(message.text)
                            }
                        }
                        override fun onError(e: Exception) {
                            binding.root.showSnackBar(e.toString())
                        }
                    })
                }
            }
        }
    }

    private fun getAudioBase64(): String? {
        val inputStream: InputStream = FileInputStream(audioFilePath)
        val myByteArray = getBytesFromInputStream(inputStream)
        return Base64.encodeToString(myByteArray, Base64.DEFAULT)
    }

    @Throws(IOException::class)
    private fun getBytesFromInputStream(`is`: InputStream): ByteArray? {
        val os = ByteArrayOutputStream()
        val buffer = ByteArray(0xFFFF)
        var len: Int = `is`.read(buffer)
        while (len != -1) {
            os.write(buffer, 0, len)
            len = `is`.read(buffer)
        }
        return os.toByteArray()
    }

    private fun showIsTyping(visibility: Boolean) {
        if (visibility) {
            binding.isTypingContainer.visibility = VISIBLE
            binding.editTxtLay.isEnabled = false
            binding.sendChat.isEnabled = false
            binding.sendVoice.isEnabled = false
        } else {
            binding.isTypingContainer.visibility = GONE
            binding.editTxtLay.isEnabled = true
            binding.sendChat.isEnabled = true
            binding.sendVoice.isEnabled = true
        }
    }

    override fun onStop() {
        speech?.destroy()
        speech = null
        recognizerIntent = null
        super.onStop()
    }
}