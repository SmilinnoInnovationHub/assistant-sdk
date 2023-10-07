package com.smilinno.projectlibrary

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import com.microsoft.signalr.TransportEnum
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Test : ComponentActivity() {

    private lateinit var hubConnection: HubConnection
    private val TAG: String = MainActivity::class.java.simpleName
    private val base64Message = "//FgQA0f/AFAIoCjemCFLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0t\n" +
            "LS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS//8WBADT/8AUAi\n" +
            "gKN6aIUtLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0t\n" +
            "LS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS//8WBADR/8AUAigKN6YIUtLS0t\n" +
            "LS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0t\n" +
            "LS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tL//xYEANP/wBQCKAo3pohS0tLS0tLS0tLS0tLS0t\n" +
            "LS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0t\n" +
            "LS0tLS0tLS0tLS0tLS0tLS0tL//xYEANP/wBQCKAo3pohS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0t\n" +
            "LS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0t\n" +
            "LS0tLS0tLS0tL//xYEANH/wA5jKYgfEKawkpdAgABPGWmv7fJB7/LOeeaqERvNBClpaWlpaWlpaW\n" +
            "lpaWlpaWlpaWlpaWlpaWlpaWlpaWlpaWlpaWlpaWlpaWlpaWlpaWlpaWlpaWlpaWlpaWlpaWlpcH\n" +
            "//FgQA6//AD8UqhMJDDBAipe7yVKkyotWRKhKsxCLYS488AZyRBgTdPhIZRQiyxpVba8QOseAcCk\n" +
            "q0VxvwcKllLxXSFpCkBKjHLX3VW8nSSYGfBi2bijISz4ACKwCgJCBZFETM+w+d6/0oQFqAJ2lEpS\n" +
            "AqBw//FgQBI//AEIm41I+lyNSBBbESkHMhK9yX1757RnPO3VeaTnf2/crnzWf0/2vejGXVf6pU3c\n" +
            "CnNOuT5nyOxmSqRNQQRHuAG0Dx3mAT+RX+YWyJBigrJ+CBqdHMNuRJREndn13HGapusL5bmdPX2a\n" +
            "6UohoqrBFBciGKgpAtLiYJChguMsFZZ4NlJ5nk91IyoAB//xYEAQ3/wBIvKH4CMam7zuUy9bXk1W\n" +
            "StG9Tdd3O9tYOxEj88TCg08IrUxJWa7c80aY72trbZRSyYVr3IUZPD3xql9qn3C60rqG4zAKbTNm\n" +
            "KTB0vjs5mKPkw3DHE7ltxiZP0eC1pWJs/+DeXdBFzeRSsY6XaTDL/1Ru/8fbzopa0WDWAAMH//Fg\n" +
            "QA/f/AEuMoRQJFjZ65lXlCQlJFRC+jPEvMawdFyI54yk1CTbDVlUAz4CEhiOy/lXbqdaV/P5kKt/\n" +
            "duJLawEmWSryxbEvxnDJeL+2x/TWlGEVrxKQ2ypp+8NHz+SUWpJWghJIABeScoCwlr/nUdCFQWw5\n" +
            "1QAQhd8HUoA4//FgQA///AFUMozsEA0MQsRToEVAIIqTOMayUSZLquHw36mgHgh/2YyZ44oHBZ69\n" +
            "Kj4okR8n+j1F+i2vilbgB2f13YsyIAH/Xo4Mw19/v93aAANRjOYC6SCdVACPR2oKn7asEygvvAC6\n" +
            "QVrJNctewCeHHK/j+FLdcs4Gud4gcP/xYEANn/wBNDKeQwAZ+yJUVJUVYXlpeq08UBIX34B997wD\n" +
            "W67/BoQiss/6fzPNzTTHG/Qfd2TlN4x1XL4chl1Wnz7ZC/BLC8d+wYsw+1SnIY/UKfcy+KKXgzAK\n" +
            "oGFjQIiGoAABGnxgjhiDB//xYEANH/wBVDKVNBENBUkCGADAul5qqkXtNbIvzrO8AAdMAW9oF66M\n" +
            "SFMKxn6mpGT5X2pqEFb8u6A3vr/hmLNffVla1pwXKobSou8wDlqGeAqSYwxjPUawQUBURAgNQQEo\n" +
            "EvpMZZjg//FgQA0//AFYMo0QhBAMWgN8e6LTdxi8fEwvXWs8LWwRZBF5fIjf/APA9TFpt1E453OB\n" +
            "QtN32u1IWcab8Roo7nAITSGSFzoS2J80S3A9JHnkRVCy10RO5hFUYnRtEgChOQgKVBcrUoDg//Fg\n" +
            "QA2//AFEMpyQhiGwBiMBtbWSlSIDzTcvfSAF1l874tyU5NCNIimWpIoiA5LwkamSzRdYsNNHBo2u\n" +
            "rJmgK83gwskFh5Kca6xdsaJKoF57nYlvfSq3CtxIT5S5pSMpOCSJEqAwpWmEEFABwP/xYEANX/wB\n" +
            "PjKVTDESDGa8RBUglX08vVxryrur9sAE47v/n25BV65SJfGtSOfTjfYI1OjfNwwcirIgc1Pswi8V\n" +
            "DmdhqmoGAAiEjqpKcKhqXVKTGIiACNYAEgF4GmgCKm8qsCLLNFwkAHA="

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSignalRHubConnection()
    }

    private fun initSignalRHubConnection() {
//        val token =
//            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJiYjU2M2FkZC00YmNjLTRmMWUtODQyZS04ODIyMTdhOGQxMjIiLCJuYmYiOjE2ODcwODIxMzMsImV4cCI6MTY4ODgxMDEzMywiaWF0IjoxNjg3MDgyMTMzLCJpc3MiOiJaaXR1cmUiLCJhdWQiOiJaaXR1cmUifQ.4dQ1R9s5AeRlaI7y79y-EgeuvK7euIv2_wO0rsSdbJU"
        val token =
            "caccc7b8-c10c-41f2-9c6f-189c75decc38"
//        val hubUrl = "http://37.32.24.190:4010/realtime/assistanthub"
        val hubUrl = "http://213.239.225.119:4060/hub"
        val accessTokenKey = "access_token"
        hubConnection = HubConnectionBuilder
            .create(hubUrl)
            .withHeader(accessTokenKey, token)
            .shouldSkipNegotiate(true)
            .withHandshakeResponseTimeout(15 * 1000)
            .withTransport(TransportEnum.WEBSOCKETS)
            .build()

        hubConnection.serverTimeout = 30 * 1000
        hubConnection.keepAliveInterval = 10 * 1000
        hubConnection.onClosed {
            Log.e(TAG, "hubConnection is closed!", it)
            setConnectionState()
            startHubConnection()
        }
        startHubConnection()
        getTextFromServer()
    }

    fun setConnectionState() {
        return if (this::hubConnection.isInitialized) {
            if (hubConnection.connectionState != HubConnectionState.CONNECTED) {
                showToast("Disconnect")
            } else {
                showToast("Connected")
            }
        } else {
            showToast("Disconnect")
        }
    }

    private fun showToast(s: String) {
        lifecycleScope.launch {
            Toast.makeText(applicationContext, s, Toast.LENGTH_LONG).show()
        }
    }

    private fun startHubConnection() {
        lifecycleScope.launch(Dispatchers.IO) {
            hubConnection.start().subscribe(object : DisposableCompletableObserver() {
                override fun onComplete() {
                    setConnectionState()
                    sendMessage(base64Message)
                }

                override fun onError(e: Throwable) {
                    setConnectionState()
                    e.message?.let { showToast(it) }
                }
            })
        }
    }

    private fun getTextFromServer() {
        hubConnection.on("UserMessage", { message: String ->
            lifecycleScope.launch {
                showToast("hubConnection on UserMessage: $message")
                Log.d(TAG, "hubConnection on UserMessage: $message")
            }
        }, String::class.java)
    }

    fun sendMessage(message: String) {
        if (message.isNotEmpty()) {
            lifecycleScope.launch(Dispatchers.IO) {
                if (isConnected()) {
                    hubConnection.send("UserMessage", message)
                }
            }
        }
    }

    private fun isConnected(): Boolean {
        return if (this::hubConnection.isInitialized) {
            hubConnection.connectionState == HubConnectionState.CONNECTED
        } else {
            false
        }
    }

}
