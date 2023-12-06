package com.smilinno.projectlibrary.ui

import PermissionManager
import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.view.View.GONE
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
import com.smilinno.smilinnolibrary.model.MessageResponse
import com.smilinno.smilinnolibrary.model.MessageType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), RecognitionListener {
    private var partialText: String = ""
    private var resultText = StringBuilder()
    private val TAG: String = MainActivity::class.java.name
    private lateinit var binding: ActivityMainNewBinding
    @Inject lateinit var assistantLibrary: AssistantLibrary
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var permissionManager: PermissionManager
    private var voice: String? = null
    private var isRecognizerActivate = false
    private var recognizerIntent: Intent? = null
    private var speech: SpeechRecognizer? = null
    private var isInternalAsr : Boolean = true

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
                    if (isInternalAsr) {
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
                    } else {
                            googleASR()
                    }

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


    //bind google

    private fun googleASR() {
        initRecognizer()
        isRecognizerActivate = if (!isRecognizerActivate) {
            speech?.startListening(recognizerIntent)
            true
        } else {
            speech?.stopListening()
            false
        }
    }


    private fun initRecognizer() {
        if (speech == null) {
            speech = SpeechRecognizer.createSpeechRecognizer(this)
            speech?.setRecognitionListener(this)
            recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            recognizerIntent?.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "fa")
            recognizerIntent?.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa")
            recognizerIntent?.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            recognizerIntent?.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            recognizerIntent?.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            recognizerIntent?.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.application.packageName)
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        binding.micAnimation.playAnimation()
        binding.micAnimationContainer.visibility = View.VISIBLE

    }

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        binding.micAnimation.pauseAnimation()
        binding.micAnimationContainer.visibility = View.INVISIBLE

    }

    override fun onError(error: Int) {
        isRecognizerActivate = false
        binding.micAnimation.pauseAnimation()
        binding.micAnimationContainer.visibility = View.INVISIBLE
        val errorMessage = getErrorText(error)
        Log.d(MainActivity::class.java.name, "FAILED: $errorMessage")
    }

    override fun onResults(results: Bundle?) {
        if (isRecognizerActivate) {
            results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let {
                if (it.isNotEmpty()) {
                    binding.chatEditText.setText(it[0])
                }
            }
        }
        binding.micAnimation.pauseAnimation()
        binding.micAnimation.progress = 0F
        binding.micAnimationContainer.visibility = View.INVISIBLE
        isRecognizerActivate = false

    }

    override fun onPartialResults(partialResults: Bundle?) {
        if (isRecognizerActivate) {
            var text = ""
            partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let {
                for (result in it) text += result.plus(" ")
            }
            binding.chatEditText.setText(text)
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {

    }

    //Makes a permission request to record audio.
    private fun getErrorText(errorCode: Int): String {
        val message: String = when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "Error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Didn't understand, please try again."
        }
        return message
    }

    override fun onStop() {
        speech?.destroy()
        speech = null
        recognizerIntent = null
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        assistantLibrary.disconnectWebSocket()
    }


}