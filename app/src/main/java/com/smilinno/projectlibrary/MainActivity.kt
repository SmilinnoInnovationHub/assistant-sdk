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
import com.microsoft.signalr.HubConnectionState
import com.smilinno.projectlibrary.databinding.ActivityMainBinding
import com.smilinno.smilinnolibrary.AssistantLibrary
import com.smilinno.smilinnolibrary.callback.SmilinnoListener
import com.smilinno.smilinnolibrary.model.MessageResponse
import com.smilinno.smilinnolibrary.model.MessageType
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
class MainActivity : AppCompatActivity() , RecognitionListener {
    private val TAG: String = MainActivity::class.java.name
    private val REQUEST_RECORD_PERMISSION = 100
    private var mRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private var timerHasFinished = true
    private var startRecordTime = 0L
    private val recordDelayTime = 800
    private lateinit var binding: ActivityMainBinding
    @Inject lateinit var assistantLibrary: AssistantLibrary
    private var voice: String? = null
    private var isRecognizerActivate = false
    private var recognizerIntent: Intent? = null
    private var speech: SpeechRecognizer? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindText()
        bindAssistant()
        showCallBack()
        bindGoogle()
    }

    //* Binds the button to the Google ASR function.
    @RequiresApi(Build.VERSION_CODES.M)
    private fun bindGoogle() {
        binding.sendGoogle.setOnClickListener {
            if (checkRecordAudioPermissionRequest()) {
                googleASR()
            }
        }
    }

    //Starts or stops the recognizer.
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

    //Initializes the recognizer and sets the recognizer intent.
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

    //Shows the call back.
    private fun showCallBack() {
        assistantLibrary.setSmilinnoCallBack(object : SmilinnoListener {
            @SuppressLint("SetTextI18n")
            override fun onMessageReceive(message: MessageResponse) {
                Log.e(TAG, "onMessageReceive: $message")
                lifecycleScope.launch(Dispatchers.Main) {
                    voice = message.voice
                    when (message.type) {
                        MessageType.MESSAGE -> {
                            binding.textView.text =
                                "${MessageType.MESSAGE.name} : ${message.text}"
                        }

                        MessageType.UNRELATED -> {
                            binding.textView2.text =
                                "${MessageType.UNRELATED.name} : ${message.text}"
                        }

                        MessageType.PAYINGTHEBILL -> {
                            binding.textView2.text =
                                "${MessageType.PAYINGTHEBILL.name} : ${message.text}"
                        }

                        MessageType.ACCOUNTBILL -> {
                            binding.textView2.text =
                                "${MessageType.ACCOUNTBILL.name} : ${message.text}"
                        }

                        MessageType.MONEYTRANSFER -> {
                            binding.textView2.text =
                                "${MessageType.MONEYTRANSFER.name} : ${message.text}"
                        }

                        MessageType.ACCOUNTBALANCE -> {
                            binding.textView2.text =
                                "${MessageType.ACCOUNTBALANCE.name} : ${message.text}"
                        }

                        else -> {}
                    }
                }
            }

            override fun onMessageError(e: Exception) {
                Log.e(TAG, "onMessageError: $e")
            }

            override fun onConnectionStateChange(connectionState: HubConnectionState) {
                Log.e(TAG, "onStateConnection: ${connectionState.name}")
                lifecycleScope.launch(Dispatchers.Main) {
                    binding.state.text = connectionState.name
                }
            }

        })
    }

    //Binds the send button to the assistant library.
    private fun bindText() {
        binding.send.setOnClickListener {
            assistantLibrary.setTextMessage(binding.editText.text.toString())
        }
    }


    // Binds the assistant to the view.
    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ClickableViewAccessibility")
    private fun bindAssistant() = with(binding.sendVoice) {
        setOnClickListener {
            binding.editText.setText("")
            if (checkRecordAudioPermissionRequest()) {
                recorderAssistant()
            }
        }
    }

    //Handles the recording of the audio.
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

    //Releases the recorder.
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

    //Initializes the MediaRecorder object.
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

    //Stops the microphone and sends the audio to the assistant.
    private fun stopMic() {
        if (isRecordAudioPermissionGranted()) {
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
                    assistantLibrary.sendVoiceMessage(audioBase64)
                }
            }
        }
    }

    //A countdown timer that is used to stop the microphone after a certain amount of time.
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

    //Checks if the RECORD_AUDIO permission is granted.
    private fun isRecordAudioPermissionGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        )
                == PackageManager.PERMISSION_GRANTED)
    }

    //Gets the audio as a base64 string.
    private fun getAudioBase64(): String? {
        val inputStream: InputStream = FileInputStream(audioFilePath)
        val myByteArray = getBytesFromInputStream(inputStream)
        return Base64.encodeToString(myByteArray, Base64.DEFAULT)
    }

    //Gets the bytes from an input stream.
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


    //Checks if the record audio permission is granted. If it is, continues running the app. If it is not, shows an alert dialog and then makes a permission request.
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

    //Shows an alert dialog to the user asking for permission to access the microphone.
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

    //Makes a permission request to record audio.
    private fun makePermissionRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_RECORD_PERMISSION
        )
    }

    override fun onReadyForSpeech(params: Bundle?) {
        binding.micAnimation2.playAnimation()
        binding.micAnimationContainer2.visibility = View.VISIBLE

    }

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        binding.micAnimation2.pauseAnimation()
        binding.micAnimationContainer2.visibility = View.INVISIBLE

    }

    override fun onError(error: Int) {
        isRecognizerActivate = false
        binding.micAnimation2.pauseAnimation()
        binding.micAnimationContainer2.visibility = View.INVISIBLE
        val errorMessage = getErrorText(error)
        Log.d(MainActivity::class.java.name, "FAILED: $errorMessage")
    }

    override fun onResults(results: Bundle?) {
        if (isRecognizerActivate) {
            results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let {
                if (it.isNotEmpty()) {
                    binding.textViewGoogle.setText(it[0])
                }
            }
        }
        binding.micAnimation2.pauseAnimation()
        binding.micAnimationContainer2.visibility = View.INVISIBLE
        isRecognizerActivate = false

    }

    override fun onPartialResults(partialResults: Bundle?) {
        if (isRecognizerActivate) {
            var text = ""
            partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let {
                for (result in it) text += result.plus(" ")
            }
            binding.textViewGoogle.setText(text)
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

}