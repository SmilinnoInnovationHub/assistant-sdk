package com.smilinno.projectlibrary.ui

import PermissionManager
import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.SpeechRecognizer
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.microsoft.signalr.HubConnectionState
import com.smilinno.projectlibrary.adapter.ChatAdapter
import com.smilinno.projectlibrary.databinding.ActivityMainNewBinding
import com.smilinno.projectlibrary.model.Chat
import com.smilinno.projectlibrary.util.hideKeyboard
import com.smilinno.projectlibrary.util.showSnackBar
import com.smilinno.smilinnolibrary.AssistantLibrary
import com.smilinno.smilinnolibrary.callback.AssistantListener
import com.smilinno.smilinnolibrary.callback.StreamVoiceListener
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
import kotlin.text.StringBuilder

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var partialText: String = ""
    private var resultText = StringBuilder()
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
        stopClick()
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
                    sendChat(binding.chatEditText.text.toString())
                    showIsTyping(true)
                    resultText.setLength(0)
                }
            } else {
                binding.root.showSnackBar("لطفا پس از اتصال مجدد تلاش کنید")
            }
        }
    }

    private fun sendChat(text : String) {
        if (text.trim().isNotEmpty()) {
            binding.root.hideKeyboard()
            assistantLibrary.sendTextToAssistant(text)
            binding.chatEditText.text?.clear()
        }
    }

    private fun stopClick() {
        binding.stopAnimationContainer.setOnClickListener {
            assistantLibrary.disconnectWebSocket()
            it.visibility = GONE
            binding.sendVoice.visibility = GONE
            binding.sendChat.visibility = VISIBLE
        }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ClickableViewAccessibility")
    private fun bindAssistant() = with(binding.sendVoice) {
        setOnClickListener {
            if (assistantLibrary.isConnected()) {
                binding.chatEditText.setText("")
                if (permissionManager.checkRecordAudioPermissionRequest(this@MainActivity)) {
                    assistantLibrary.startWebSocket(this@MainActivity,
                        object : StreamVoiceListener {

                            override fun onReadyForSpeech() {
                                visibility = GONE
                                binding.sendChat.visibility = GONE
                                binding.stopAnimationContainer.visibility = View.VISIBLE
                            }

                            override fun onEndOfSpeech(reason: String) {
                                if (binding.chatEditText.text?.isEmpty() == true){
                                    visibility = VISIBLE
                                    binding.sendChat.visibility = GONE
                                    binding.stopAnimationContainer.visibility = View.GONE
                                } else {
                                    visibility = GONE
                                    binding.sendChat.visibility = VISIBLE
                                    binding.stopAnimationContainer.visibility = View.GONE
                                }
                            }

                            override fun onError(e: Throwable) {

                            }

                            override fun onResults(text: String) {
                                resultText = StringBuilder().append(resultText).append(text).append(" ")
                                visibility = GONE
                                binding.sendChat.visibility = GONE
                                binding.stopAnimationContainer.visibility = View.VISIBLE
                            }

                            @SuppressLint("SetTextI18n")
                            override fun onPartialResults(text: String) {
                                partialText = text
                                binding.chatEditText.setText("$resultText $partialText")
                                visibility = GONE
                                binding.sendChat.visibility = GONE
                                binding.stopAnimationContainer.visibility = View.VISIBLE
                            }

                        })
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
                    resultText.setLength(0)
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

    override fun onDestroy() {
        super.onDestroy()
        assistantLibrary.disconnectWebSocket()
    }

    //    private fun setupWebSocket() {
//        binding.send.setOnClickListener {
//            if (permissionManager.checkRecordAudioPermissionRequest(this@MainActivity)) {
//                assistantLibrary.startWebSocket(this,object : StreamVoiceListener{
//                    override fun onReadyForSpeech() {
//
//                    }
//
//                    override fun onEndOfSpeech(reason: String) {
//
//                    }
//
//                    override fun onError(e: Throwable) {
//
//                    }
//
//                    override fun onResults(text: String) {
//                        resultText = StringBuilder().append(resultText).append(text).append(" ")
//                        Log.e(TAG, "resultText: $resultText ", )
//                    }
//
//                    @SuppressLint("SetTextI18n")
//                    override fun onPartialResults(text: String) {
//                        partialText = text
//                        Log.e(TAG, "onPartialResults: $partialText", )
//                        binding.chatEditText.setText(" $resultText $partialText ")
//                    }
//
//                })
//            }
//        }
//    }

}