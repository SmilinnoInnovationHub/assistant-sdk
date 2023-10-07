package com.smilinno.projectlibrary

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
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.smilinno.projectlibrary.databinding.ActivityMainBinding
import com.smilinno.smilinnolibrary.SmilinnoLibrary
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.ArrayList
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val REQUEST_RECORD_PERMISSION = 100
    private var mRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private var timerHasFinished = true
    private var isRecognizerActivate = false
    private var startRecordTime = 0L
    private val recordDelayTime = 800
    private var clearContextTime = 0L
    private var delayClearContext = 5000
    private var speech: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null
    private var chatId: String? = null
    private lateinit var binding: ActivityMainBinding
    @Inject
    lateinit var smilinnoLibrary: SmilinnoLibrary
    private val base64Message =
        "//FgQA0f/AFAIoCjemCFLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0t\n" +
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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.send.setOnClickListener {
            smilinnoLibrary.sendVoiceMessage(base64Message)
        }
        binding.send.setOnClickListener {
            smilinnoLibrary.setTextMessage(binding.editText.text.toString())
        }
//        bindAssistant()
    }



    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ClickableViewAccessibility")
    private fun bindAssistant() = with(binding.send) {
        setOnClickListener {
            if (checkRecordAudioPermissionRequest()) {
                recorderAssistant()
            }
        }
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
            Log.e("", "releaseRecorder: ", e)
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
        if (isRecordAudioPermissionGranted()) {
            binding.micAnimationContainer.visibility = View.GONE
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
                    smilinnoLibrary.sendVoiceMessage(audioBase64)
                }
            }
        }
    }

    private var cTimer: CountDownTimer? = object :
        CountDownTimer(11000, 1000) {
        override fun onTick(millisUntilFinished: Long) {

        }

        override fun onFinish() {
            timerHasFinished = true
            binding.micAnimationContainer.visibility = View.GONE
            stopMic()
        }
    }

    private fun isRecordAudioPermissionGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        )
                == PackageManager.PERMISSION_GRANTED)
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


    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkRecordAudioPermissionRequest(): Boolean {
        return if (isRecordAudioPermissionGranted()) {
            // continue running app
            true
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
            showAlertDialog(this)
            false
        } else {
            //            dismiss()
            makePermissionRequest()
            false
        }
    }

    private fun showAlertDialog(context: Context?) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setMessage("voice_permission")
        alertDialogBuilder.setPositiveButton("acceptBtn") { _, _ ->
            makePermissionRequest()
        }
        alertDialogBuilder.setNegativeButton("cancelBTN") { dialog, which ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun makePermissionRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_RECORD_PERMISSION
        )
    }

}