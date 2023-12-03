package com.smilinno.smilinnolibrary.util

import android.app.Activity
import android.app.Application
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
import kotlinx.coroutines.withContext
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
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.experimental.and


internal object WebSocketClient {
    private lateinit var recordChunkAudio: ByteArray
    private lateinit var webSocket: WebSocket
    var streamVoiceListener: StreamVoiceListener? = null
    private var isRecording = false
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    private val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize)
    private lateinit var recordingJob: Job


    private val client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(10, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    fun connect(token: String, activity: Activity) {
        val request = Request.Builder()
            .url(ASR_HUB)
            .addHeader(AUTHORIZATION, token)
            .build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                Log.e("WebSocketClient", "WebSocket connected to $ASR_HUB")
                Log.e("WebSocketClient", "WebSocket connected to code : ${response.code} ,body : ${response.body} ,message : ${response.message}")
                streamVoiceListener?.onReadyForSpeech()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                Log.e("WebSocketClient", "onMessage: ${text.unescapeUnicode()} ")
                val subject = parseJson<Subject>(text)
                // check the subject of the message
                when (subject.subject) {

                    INIT -> {
                        // if init, it means server is ready for a new session
                        Log.e("WebSocketClient", "INIT: Initiated Successfully...$text")
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
                        Log.e("WebSocketClient", "REQ_RESPONSE...$text")
                        val subjectSoes = SubjectSoeS(subject = "SOES")
                        // we send request for a new session
                        val json = Gson().toJson(subjectSoes)
                        webSocket.send(json)

                    }

                    SOES_RESPONSE -> {
                        // here session is started
                        Log.e("WebSocketClient", "SOES_RESPONSE : Session Started...$text")
                        startRecording(activity)
                    }

                    NO_USAGE_REMAINED -> {
                        // if you dont have credit to record microphone
                        Log.e("WebSocketClient", "NO_USAGE_REMAINED : Your credit has ended...$text")
                        stopRecording()
                    }
                }
                streamVoiceListener?.onResults(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                super.onMessage(webSocket, bytes)
                Log.e("WebSocketClient", "onMessage: ${bytes.hex()}")
                streamVoiceListener?.onPartialResults(bytes.hex())
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                Log.e("WebSocketClient", "onClosing: $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                Log.e("WebSocketClient", "onClosed: $reason")
                streamVoiceListener?.onEndOfSpeech(reason)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                Log.e("WebSocketClient", "onFailure: ${t.message}")
                streamVoiceListener?.onError(t)
            }
        })
    }

    private fun startRecording(activity: Activity) {
        audioRecord.startRecording()
        isRecording = true
        val buffer = ByteArray(bufferSize) // 16-bit PCM encoding, so 2 bytes per sample
        recordChunkAudio = buffer
        val audioFilePath = "${activity.getExternalFilesDir(null)?.absolutePath}/222222222.pcm"
        val file = File(audioFilePath)
        val outputStream = BufferedOutputStream(FileOutputStream(file))
        // Coroutine for reading audio data
        recordingJob  = CoroutineScope(Dispatchers.IO).launch {
            while (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                val bytesRead = audioRecord.read(buffer, 0, bufferSize)
                if (bytesRead > 0) {
                    recordChunkAudio += buffer
                    outputStream.write(buffer)
                }
            }
            outputStream.close()
        }
        sendChunk()
    }

    private fun sendChunk() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(500)
            webSocket.send(recordChunkAudio.toByteString())
            recordChunkAudio = ByteArray(bufferSize)
            if (isRecording){
                sendChunk()
            }
        }
    }

    private fun stopRecording() {
        isRecording = false
        val subjectSoes = SubjectSoeS(subject = "EOES")
        val jsonEoes = Gson().toJson(subjectSoes)
        webSocket.send(jsonEoes)
        val subjectReQ = SubjectReQ(subject = "REQ", mode = "stream-custom-raw", guid = "", engine = "1", decodingInfo = SubjectReQ.DecodingInfo(sampleRate = 16000, refText = ""))
        val jsonReq = Gson().toJson(subjectReQ)
        webSocket.send(jsonReq)
        recordingJob.cancel()  // Cancel the recording coroutine
        audioRecord.stop()
        audioRecord.release()
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

}