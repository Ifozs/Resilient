package tech.resilientgym.resilient

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.zxing.integration.android.IntentIntegrator

class MainActivity : ComponentActivity() {
    private lateinit var userSessionManager: UserSessionManager
    private lateinit var barcodeScanner: BarcodeScanner
    private var barcodeResult by mutableStateOf("")
    private val scanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val scanResult = IntentIntegrator.parseActivityResult(result.resultCode, data)

            if (scanResult != null) {
                if (scanResult.contents != null) {
                    barcodeResult = scanResult.contents
                }
            }

            if (scanResult != null) {
                if (scanResult.contents == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
                } else {
                    // TODO: Use the scanned barcode value
                    Toast.makeText(this, "Scanned: " + scanResult.contents, Toast.LENGTH_LONG).show()
                }
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize UserSessionManager with the context of MainActivity
        userSessionManager = UserSessionManager(context = this)
        barcodeScanner = BarcodeScanner(this, scanLauncher)
        setContent {
            App(userSessionManager = userSessionManager, barcodeScanner, barcodeResult)
        }
    }

    override fun onStop() {
        super.onStop()
        // Log out the user when the app goes into the background
        userSessionManager.logout()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Log out the user when the app is closed
        userSessionManager.logout()
    }

}
