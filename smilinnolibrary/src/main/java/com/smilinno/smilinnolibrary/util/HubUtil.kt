package com.smilinno.smilinnolibrary.util

import android.annotation.SuppressLint
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import com.microsoft.signalr.TransportEnum
import com.smilinno.smilinnolibrary.apistate.ApiState
import com.smilinno.smilinnolibrary.callback.SmilinnoListener
import com.smilinno.smilinnolibrary.model.Chat
import com.smilinno.smilinnolibrary.model.CreatedDate
import com.smilinno.smilinnolibrary.model.ResponseChatId
import com.smilinno.smilinnolibrary.model.type.ChatType
import com.smilinno.smilinnolibrary.model.type.DeliverType
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import kotlinx.coroutines.flow.*
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


internal object HubUtil {
    private lateinit var hubConnection: HubConnection
    var smilinnoListener: SmilinnoListener? = null
        set(value) {
            field = value
            sendConnectionState()
        }
    private val ACCESS_TOKEN_KEY = "access_token"
    private val TEXTMESSAGE = "TextMessage"
    private val CLEARCONTEXT = "ClearContext"
    private val RESENDMESSAGE = "ResendMessage"
    private val GetAcknowledge = "GetAcknowledge"
    private val AttachChatId = "AttachChatId"
    private val STOPMESSAGE = "StopMessage"
    private val VOICEMESSAGE = "UserMessage"
    private val MESSAGE = "Message"
    // private val MESSAGE = "Message"
    // private val MESSAGE = "UserMessage"
    // private val MESSAGE = "UnRelated"
    private val UNAUTHORIZED = "Unauthorized"
    private val FORBIDDEN = "Forbidden"
    private val ADVERTISEMENT = "Advertisment"
    private val GENERATEIMAGE = "GenerateImage"
    const val ITEMS_PER_PAGE = 10
    const val REQUEST_HISTORY_MESSAGE = "SendUserChatHistory"//todo
    const val RESPONSE_HISTORY_MESSAGE = "GetUserChatHistory"
    var pageSize: Int = ITEMS_PER_PAGE
    private val TAG: String = HubUtil::class.java.simpleName
    private var lastServerMessageId = ""
    private var ignorServerMessageId = ""
    private var ignorServerMessage = false
    var currentChatType: ChatType = ChatType.Text
    var chatHistory: ArrayList<Chat>? = null
    var pageId: String? = null

    private val chatStateFlow: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    val _chatStateFlow: StateFlow<ApiState> = chatStateFlow

    private val connectionStateFlow: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    val _connectionStateFlow: StateFlow<ApiState> = connectionStateFlow

    private val chatHistoryStateFlow: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    val _chatHistoryStateFlow: StateFlow<ApiState> = chatHistoryStateFlow

    private val deleteHistoryStateFlow: MutableStateFlow<ApiState> =
        MutableStateFlow(ApiState.Empty)
    val _deleteHistoryStateFlow: StateFlow<ApiState> = deleteHistoryStateFlow


    fun initSignalRHubConnection(token: String) {
        try {
            hubConnection = HubConnectionBuilder
//                http://37.32.24.190:9104/chathub_v1_5
//                https://assistant.smilinno-dev.com/hub
                .create("http://37.32.24.190:9104/chathub_v1_5")
                .withHeader(ACCESS_TOKEN_KEY, token)
                .shouldSkipNegotiate(true)
                .withHandshakeResponseTimeout(15 * 1000)
                .withTransport(TransportEnum.WEBSOCKETS)
                .build()
            hubConnection.serverTimeout = 30 * 1000
            hubConnection.keepAliveInterval = 10 * 1000
            hubConnection.onClosed {
                Log.e(TAG, "hubConnection is closed!", it)
                sendConnectionState()
                startHubConnection()
            }
            getTextFromServer()
            getAcknowledgeFromServer()
            getChatIdFromServer()
            getAdvertisementFromServer()
            getForbiddenFromServer()
            getUnauthorizedFromServer()
            listenToGetHistory()
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "initSignalRHubConnection exception:", e)
        }

    }

    fun startHubConnection() {

            try {
                hubConnection.start().subscribe(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        ignorServerMessage = false
                        sendConnectionState()
                        sendLastChatId()
                        requestToGetHistory(pageId, pageSize)
                    }

                    override fun onError(e: Throwable) {
                            sendConnectionState()
//                            delay(5000)
                        Executors.newSingleThreadScheduledExecutor().schedule({
                            startHubConnection()
                        }, 5, TimeUnit.SECONDS)
                    }
                })
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "hubConnection start: ", e)
            }

    }

    fun isConnected(): Boolean {
        return if (this::hubConnection.isInitialized) {
            hubConnection.connectionState == HubConnectionState.CONNECTED
        } else {
            false
        }
    }

    fun sendConnectionState() {
       smilinnoListener?.onConnectionStateChange(hubConnection.connectionState)
    }

    private fun getUnauthorizedFromServer() {
        try {
            hubConnection.on(UNAUTHORIZED, { message: String ->
//                MainRepository.application.goToLogin()
            }, String::class.java)
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "hubConnection on: ", e)
        }
    }

    private fun getAcknowledgeFromServer() {
        try {
            hubConnection.on(GetAcknowledge, { id: String ->
                chatStateFlow.value = ApiState.Success(id)
            }, String::class.java)
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "hubConnection on: ", e)
        }
    }

    private fun getChatIdFromServer() {
        try {
            hubConnection.on(AttachChatId, { requestId: String, chatId: String ->
                chatStateFlow.value = ApiState.Success(ResponseChatId(requestId, chatId))
            }, String::class.java, String::class.java)
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "hubConnection on: ", e)
        }
    }

    private fun getForbiddenFromServer() {
        try {
            hubConnection.on(FORBIDDEN, { message: String ->
//                MainRepository.application.goToLogin()
            }, String::class.java)
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "hubConnection on: ", e)
        }
    }

    private fun getTextFromServer() {
        try {
            hubConnection.on(MESSAGE, { message: String ->
                    Log.d(TAG, "hubConnection on ARG_MESSAGE: $message")
                    smilinnoListener?.onMessageReceive(message)

            }, String::class.java)
        } catch (e: java.lang.Exception) {
            smilinnoListener?.onMessageError(e)
            Log.e(TAG, "hubConnection on: ", e)
        }
    }

    private fun getAdvertisementFromServer() {
        try {
            hubConnection.on(ADVERTISEMENT, { it ->
//                chatStateFlow.value = ApiState.Success(it)
                Log.d(TAG, "hubConnection Advertisement on: $it")
//                AdUtils.showInterstitialAd()
            }, String::class.java)
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "hubConnection on: ", e)
        }
    }

    fun sendLastChatId() {
        if (lastServerMessageId.isNotEmpty()) {

                if (isConnected()) {
                    try {
                        Log.d(TAG, "hubConnection resend! $lastServerMessageId")
                        hubConnection.send(RESENDMESSAGE, lastServerMessageId)
                    } catch (e: java.lang.Exception) {
                        Log.e(TAG, "hubConnection send error: ", e)
                    }
                }

        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun sendStopMessage() {
        ignorServerMessage = true
        if (lastServerMessageId.isNotEmpty()) {
            ignorServerMessageId = lastServerMessageId
                if (isConnected()) {
                    try {
                        Log.d(TAG, "hubConnection StopMessage! $lastServerMessageId")
                        hubConnection.send(STOPMESSAGE, lastServerMessageId)
                    } catch (e: java.lang.Exception) {
                        Log.e(TAG, "hubConnection send error: ", e)
                    }
                }

        }
    }

    fun sendVoiceChat(voiceBase64String: String) {

            lastServerMessageId = ""
            chatStateFlow.value = ApiState.Loading
            if (!isConnected()) {
//                delay(1000)
                Executors.newSingleThreadScheduledExecutor().schedule({
                 sendVoiceChat(voiceBase64String)
                }, 1, TimeUnit.SECONDS)
            } else {
                try {
                    Log.d(TAG, "hubConnection sent!")
                    hubConnection.send(VOICEMESSAGE, voiceBase64String)
                } catch (e: java.lang.Exception) {
                    Log.e(TAG, "hubConnection send error: ", e)
                }
            }

    }

    fun sendChat(text: String) {
        when (currentChatType) {
            ChatType.Text -> {
                sendTextChat(text)
            }

            ChatType.Image -> {
                sendGenerateImage(text)
            }

            else -> {}
        }
    }

    fun sendClearContextChat() {

            if (!isConnected()) {
//                delay(1000)
                Executors.newSingleThreadScheduledExecutor().schedule({
                sendClearContextChat()
                }, 1, TimeUnit.SECONDS)

            } else {
                try {
                    Log.d(TAG, "hubConnection sent ClearContext!")
                    hubConnection.send(CLEARCONTEXT)
                } catch (e: java.lang.Exception) {
                    Log.e(TAG, "hubConnection send error: ", e)
                }
            }

    }


    fun sendTextChat(text: String) {

            chatStateFlow.value = ApiState.Loading
            if (!isConnected()) {
//                delay(1000)
            smilinnoListener?.onMessageError(Exception("Server ${hubConnection.connectionState}"))

            } else {
                try {
                    Log.d(TAG, "hubConnection sent!")
                    val chat = getClientChat(text)
                    lastServerMessageId = chat.id.toString()
                    hubConnection.send(TEXTMESSAGE, text, chat.id)
                    chatStateFlow.value = ApiState.Success(chat)
                } catch (e: java.lang.Exception) {
                    Log.e(TAG, "hubConnection send error: ", e)
                }
            }

    }

    private fun sendGenerateImage(text: String) {

            chatStateFlow.value = ApiState.Loading
            if (!isConnected()) {
//                delay(1000)
                Executors.newSingleThreadScheduledExecutor().schedule({
                    sendGenerateImage(text)
                }, 1, TimeUnit.SECONDS)

            } else {
                try {
                    Log.d(TAG, "hubConnection sent!")
                    val chat = getClientChat(text)
                    lastServerMessageId = chat.id.toString()
                    hubConnection.send(GENERATEIMAGE, text, chat.id)
                    chatStateFlow.value = ApiState.Success(chat)
                } catch (e: java.lang.Exception) {
                    Log.e(TAG, "hubConnection send error: ", e)
                }
            }

    }

    private fun getClientChat(text: String): Chat {
        val createdDate = CreatedDate(TimeUtils.getRawServerTime(), null, null)
        return Chat(
            id = UUID.randomUUID().toString(),
            text = text,
            isAssistant = false,
            time = TimeUtils.getServerTime(),
            createdOn = createdDate
        )
    }

    private fun parsObjectIntoChat(jsonString: String): Chat? {
        val gson = Gson()
        val chat = gson.fromJson(jsonString, Array<Chat>::class.java)[0]
        when (chat.type) {
            "error" -> {
                chat.isAssistant = true
                chat.isError = true
            }

            "server" -> {
                chat.isAssistant = true
            }

            "user" -> {
                chat.isAssistant = false
            }

            "clearcontext" -> {
                chat.responseType = ChatType.CLEAR_CONTEXT
                chat.isAssistant = true
            }
        }
        chat.createdOn?.raw?.let {
            TimeUtils.parseTime(it)?.time?.let { time ->
                chat.time = time
            }
        }
        return if (chat.isAssistant) {
            chat
        } else {
            null
        }
    }

    private fun parsObjectIntoChatList(jsonString: String): ArrayList<Chat> {
        val gson = Gson()
        val list: ArrayList<Chat> =
            gson.fromJson(jsonString, object : TypeToken<ArrayList<Chat?>?>() {}.type)
        for (chat in list) {
            when (chat.type) {
                "error" -> {
                    chat.isAssistant = true
                    chat.isError = true
                }

                "server" -> {
                    chat.isAssistant = true
                }

                "user" -> {
                    chat.isAssistant = false
                }

                "clearcontext" -> {
                    chat.responseType = ChatType.CLEAR_CONTEXT
                    chat.isAssistant = true
                }
            }

            chat.createdOn?.raw?.let {
                TimeUtils.parseTime(it)?.time?.let { time ->
                    chat.time = time
                }
            }

            chat.deliverType = DeliverType.DELIVER
        }
        return list
    }

    fun requestToGetHistory(pageId: String?, pageSize: Int?) {
        Log.d(TAG, "requestToGetHistory! pageId $pageId to $pageSize")

            Log.d(TAG, "requestToGetHistory! IO")
            if (isConnected()) {
                Log.d(TAG, "requestToGetHistory! hub connected")
                try {
                    Log.d(TAG, "hubConnection requestToGetHistory! try sending")
                    chatHistoryStateFlow.value = ApiState.Loading
                    hubConnection.send(REQUEST_HISTORY_MESSAGE, pageId)//server has pageSize
                } catch (e: java.lang.Exception) {
                    Log.e(TAG, "hubConnection requestToGetHistory error: ", e)
                    chatHistoryStateFlow.value = ApiState.Failure(e)
                }
            }

    }

    private fun listenToGetHistory() {

            try {
                hubConnection.on(RESPONSE_HISTORY_MESSAGE, { message: String ->
                    Log.d(TAG, "hubConnection on listenToGetHistory: $message")
                    chatHistory = parsObjectIntoChatList(message)
                    pageSize = chatHistory?.size!!
                    parsObjectIntoChatList(message).let {
                        chatHistoryStateFlow.value = ApiState.Success(it)
                    } ?: kotlin.run {
                        chatHistoryStateFlow.value =
                            ApiState.Failure(Exception("null chatHistory list"))
                    }

                }, String::class.java)
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "hubConnection listenToGetHistory on: ", e)
                chatHistory = null
                chatHistoryStateFlow.value = ApiState.Failure(e)
            }

    }

}