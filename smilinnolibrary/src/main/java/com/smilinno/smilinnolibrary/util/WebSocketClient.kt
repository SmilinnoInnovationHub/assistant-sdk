package com.smilinno.smilinnolibrary.util

import android.app.Activity
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.smilinno.smilinnolibrary.callback.StreamVoiceListener
import com.smilinno.smilinnolibrary.model.Subject
import com.smilinno.smilinnolibrary.model.SubjectReQ
import com.smilinno.smilinnolibrary.model.SubjectSoeS
import com.smilinno.smilinnolibrary.model.TextPartial
import com.smilinno.smilinnolibrary.model.TextResult
import com.smilinno.smilinnolibrary.util.Constants.ASR_HUB
import com.smilinno.smilinnolibrary.util.Constants.AUTHORIZATION
import com.smilinno.smilinnolibrary.util.Constants.INIT
import com.smilinno.smilinnolibrary.util.Constants.NO_USAGE_REMAINED
import com.smilinno.smilinnolibrary.util.Constants.REQ_RESPONSE
import com.smilinno.smilinnolibrary.util.Constants.SOES_RESPONSE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.logging.HttpLoggingInterceptor
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.LinkedList
import java.util.concurrent.TimeUnit


internal object WebSocketClient {
    private lateinit var recordChunkAudio: ByteArray
    private var webSocket: WebSocket? = null
    var streamVoiceListener: StreamVoiceListener? = null
    private var isRecording = false
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    private var audioRecord : AudioRecord? = null
    private var recordingJob: Job? = null
    private var isConnected: Boolean = false
    private var chunkFifoList: LinkedList<ByteArray> = LinkedList<ByteArray>()

    /**
     * This class represents a custom configuration for OkHttpClient.
     * It includes settings such as read timeout, ping interval, and logging interceptor.
     *
     * @property client The configured OkHttpClient instance.
     *
     * @constructor Creates an instance of [CustomHttpClient].
     */
    private val client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(10, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    /**
     * Connects to the ASR server using the provided access token and activity.
     *
     * @param token The access token for authentication.
     * @param activity The activity context associated with the WebSocket connection.
     */
    fun connect(token: String, activity: Activity) {
        val request = Request.Builder()
            .url(ASR_HUB)
            .addHeader(AUTHORIZATION, token)
            .build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                isConnected = true
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                Log.e("WebSocketClient", "onMessage1: ${text.unescapeUnicode()} ")
                val parsTextToSubject = parseJson<Subject>(text)
                val parsTextToPartial = parseJson<TextPartial>(text)
                val parsTextToResult = parseJson<TextResult>(text)
                // check the subject of the message
                when (parsTextToSubject.subject) {

                    INIT -> {
                        // if init, it means server is ready for a new session
                        Log.e("WebSocketClient", "onMessage1 INIT: Initiated Successfully...$text")
                        val subjectReQ = SubjectReQ(
                            subject = "REQ",
                            mode = "stream-custom-raw",
                            guid = "",
                            engine = "1",
                            decodingInfo = SubjectReQ.DecodingInfo(sampleRate = 16000, refText = "")
                        )
                        // we send request for a new session
                        val json = Gson().toJson(subjectReQ)
                        webSocket.send(json)
                    }

                    REQ_RESPONSE -> {
                        // in response for our session request, server returns req-response and it's ready to recieve microphone chunks
                        // we send start of engine session to start recording
                        Log.e("WebSocketClient", "onMessage1 REQ_RESPONSE...$text")
                        val subjectSoes = SubjectSoeS(subject = "SOES")
                        // we send request for a new session
                        val json = Gson().toJson(subjectSoes)
                        webSocket.send(json)
                        CoroutineScope(Dispatchers.Main).launch {
                            streamVoiceListener?.onReadyForSpeech()
                        }
                    }

                    SOES_RESPONSE -> {
                        // here session is started
                        Log.e("WebSocketClient", "onMessage1 SOES_RESPONSE : Session Started...$text")

                    }

                    NO_USAGE_REMAINED -> {
                        // if you dont have credit to record microphone
                        Log.e("WebSocketClient", "onMessage1 NO_USAGE_REMAINED : Your credit has ended...$text")
                        stopRecording()
                    }
                }

                // check if there is a transcription in response
                // check if transcription partial is available, ( to show in your app in realtime )
                CoroutineScope(Dispatchers.Main).launch {
                    if (parsTextToPartial.transcription?.partial.isNullOrEmpty().not()) {
                        parsTextToPartial.transcription?.partial?.let {
                            streamVoiceListener?.onPartialResults(it)
                        }
                    }
                }

                // check if there is a final result in response
                CoroutineScope(Dispatchers.Main).launch {
                    if (parsTextToResult.transcription?.result.isNullOrEmpty().not()) {
                        parsTextToResult.transcription?.text?.let {
                            streamVoiceListener?.onResults(it)
                        }
                    }
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                super.onMessage(webSocket, bytes)
                Log.e("WebSocketClient", "onMessage2: ${bytes.hex()}")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                isConnected = false
                Log.e("WebSocketClient", "onClosing: $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                Log.e("WebSocketClient", "onClosed: $reason")
                CoroutineScope(Dispatchers.Main).launch {
                    streamVoiceListener?.onEndOfSpeech(reason)
                }
                isConnected = false
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                Log.e("WebSocketClient", "onFailure: ${t.message}")
                CoroutineScope(Dispatchers.Main).launch {
                    streamVoiceListener?.onError(t)
                }
                isConnected = false
            }
        })
    }

    /**
     * This data class represents a recording utility that captures audio input from the device's microphone
     * and saves it to a PCM file. It utilizes Android's AudioRecord API for audio capturing.
     *
     * @property sampleRate The sample rate of the audio recording.
     * @property channelConfig The configuration of audio channels (e.g., mono or stereo).
     * @property audioFormat The audio format used for recording (e.g., 16-bit PCM encoding).
     * @property bufferSize The size of the buffer used for audio recording.
     *
     * @constructor Creates an instance of the recording utility with the specified parameters.
     *
     * @param sampleRate The sample rate of the audio recording.
     * @param channelConfig The configuration of audio channels (e.g., mono or stereo).
     * @param audioFormat The audio format used for recording (e.g., 16-bit PCM encoding).
     * @param bufferSize The size of the buffer used for audio recording.
     */
     fun startRecording(activity: Activity) {
        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize)
        audioRecord?.startRecording()
        isRecording = true
        val buffer = ByteArray(bufferSize) // 16-bit PCM encoding, so 2 bytes per sample
        recordChunkAudio = buffer
        val audioFilePath = "${activity.getExternalFilesDir(null)?.absolutePath}/222222222.pcm"
        val file = File(audioFilePath)
        val outputStream = BufferedOutputStream(FileOutputStream(file))
        // Coroutine for reading audio data
        recordingJob  = CoroutineScope(Dispatchers.IO).launch {
            while (isRecording) {
                val bytesRead = audioRecord?.read(buffer, 0, bufferSize)
                if (bytesRead != null) {
                    if (bytesRead > 0) {
                        recordChunkAudio += buffer
                        outputStream.write(buffer)
                    }
                }
            }
            outputStream.close()
        }
        createChunk()
        sendChunk()
    }

    /**
     * Sends an audio chunk over the WebSocket in a background coroutine.
     *
     * The method launches a coroutine in the IO dispatcher, delays for 500 milliseconds,
     * sends the audio chunk, resets the chunk buffer, and recursively calls itself if
     * recording is still in progress.
     */
    private fun sendChunk() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(100)
            if (isConnected) {
                if (chunkFifoList.size > 0) {
                    val firstChunk = chunkFifoList.peek()
                    webSocket?.send(firstChunk.toByteString())
                    chunkFifoList.remove()
                }
            }
            if (isRecording){
                sendChunk()
            }
        }
    }

    private fun createChunk() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(500)
            chunkFifoList.add(recordChunkAudio)
            recordChunkAudio = ByteArray(bufferSize)
            if (isRecording){
                createChunk()
            }
        }
    }

    /**
     * Stops the ongoing audio recording.
     *
     * This method sets the [isRecording] flag to false, sends recorded data over the WebSocket connection
     * in the form of JSON, and cancels the recording coroutine. It also stops and releases the AudioRecord instance.
     */
    fun stopRecording() {
        isRecording = false
        val subjectSoes = SubjectSoeS(subject = "EOES")
        val jsonEoes = Gson().toJson(subjectSoes)
        webSocket?.send(jsonEoes)
        val subjectReQ = SubjectReQ(subject = "REQ", mode = "stream-custom-raw", guid = "", engine = "1", decodingInfo = SubjectReQ.DecodingInfo(sampleRate = 16000, refText = ""))
        val jsonReq = Gson().toJson(subjectReQ)
        webSocket?.send(jsonReq)
        recordingJob?.cancel()  // Cancel the recording coroutine
        audioRecord?.stop()
        audioRecord?.release()
    }


    inline fun <reified T> parseJson(json: String): T {
        return Gson().fromJson(json, object : TypeToken<T>() {}.type)
    }

    fun String.unescapeUnicode(): String {
        val regex = Regex("\\\\u([0-9a-zA-Z]{4})")
        return replace(regex) { result ->
            val hexCode = result.groupValues[1]
            val decimalValue = hexCode.toInt(16)
            decimalValue.toChar().toString()
        }
    }

    /**
     * Disconnects the WebSocket, stops recording, and performs cleanup operations.
     * Specifically, it sends EOES and REQ messages, cancels the recording coroutine,
     * and closes the WebSocket connection along with releasing the AudioRecord resources.
     */
    fun disconnectWebSocket() {
        isRecording = false
        val subjectSoes = SubjectSoeS(subject = "EOES")
        val jsonEoes = Gson().toJson(subjectSoes)
        webSocket?.send(jsonEoes)
        val subjectReQ = SubjectReQ(subject = "REQ", mode = "stream-custom-raw", guid = "", engine = "1", decodingInfo = SubjectReQ.DecodingInfo(sampleRate = 16000, refText = ""))
        val jsonReq = Gson().toJson(subjectReQ)
        webSocket?.send(jsonReq)
        recordingJob?.cancel()  // Cancel the recording coroutine
        webSocket?.close(1000, "Closing the connection")
        if (audioRecord?.state == AudioRecord.STATE_INITIALIZED){
            audioRecord?.stop()
            audioRecord?.release()
        }
    }

}