package com.smilinno.projectlibrary

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.microsoft.signalr.HubConnectionState
import com.skydoves.balloon.Balloon
import com.smilinno.projectlibrary.databinding.ActivityMainBinding
import com.smilinno.smilinnolibrary.SmilinnoLibrary
import com.smilinno.smilinnolibrary.callback.SmilinnoListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val REQUEST_RECORD_PERMISSION = 100
    private var mRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private var timerHasFinished = true
    private var startRecordTime = 0L
    private val recordDelayTime = 800
    private lateinit var binding: ActivityMainBinding
    @Inject lateinit var smilinnoLibrary: SmilinnoLibrary

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindText()

        smilinnoLibrary.setSmilinnoCallBack(object : SmilinnoListener {

            override fun onMessageReceive(message: String) {
                Log.e("3535", "onMessageReceive: $message", )
            }

            override fun onMessageError(e: Exception) {
                Log.e("3535", "onMessageError: $e", )
            }

            override fun onConnectionStateChange(connectionState: HubConnectionState) {
                Log.e("3535", "onStateConnection: ${connectionState.name}", )
            }

        })
//        bindAssistant()
    }

    private fun bindText() {
        binding.send.setOnClickListener {
            smilinnoLibrary.setTextMessage(binding.editText.text.toString())
        }
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