import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager(private val context: Context) {

    companion object {
        const val REQUEST_RECORD_PERMISSION = 100 // Use your own request code
    }

    fun checkRecordAudioPermissionRequest(activity: AppCompatActivity): Boolean {
        return if (isRecordAudioPermissionGranted()) {
            // Continue running app
            true
        } else if (shouldShowRequestPermissionRationale(activity,Manifest.permission.RECORD_AUDIO)) {
            showAlertDialog(activity)
            false
        } else {
            makePermissionRequest(activity)
            false
        }
    }

    fun isRecordAudioPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun makePermissionRequest(activity: AppCompatActivity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_RECORD_PERMISSION
        )
    }

    private fun shouldShowRequestPermissionRationale(activity: AppCompatActivity,permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity,permission)
    }

    private fun showAlertDialog(activity: AppCompatActivity) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setMessage("voice_permission")
        alertDialogBuilder.setPositiveButton("acceptBtn") { _, _ ->
            makePermissionRequest(activity)
        }
        alertDialogBuilder.setNegativeButton("cancelBTN") { dialog, which ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}
