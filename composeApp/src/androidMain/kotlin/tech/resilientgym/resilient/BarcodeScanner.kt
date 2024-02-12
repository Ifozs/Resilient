package tech.resilientgym.resilient

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ComponentActivity
import com.google.zxing.integration.android.IntentIntegrator

// In androidMain
actual class BarcodeScanner(
    private val activity: ComponentActivity,
    private val scanLauncher: ActivityResultLauncher<Intent>
) {
    actual fun startCamera() {
        val integrator = IntentIntegrator(activity).apply {
            setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            setPrompt("Scan a barcode")
            setCameraId(0)
            setBeepEnabled(false)
            setBarcodeImageEnabled(true)
        }
        val intent = integrator.createScanIntent()
        scanLauncher.launch(intent)
    }

    actual fun stopCamera() {
        // Implement any required functionality to stop the camera
    }

    actual fun setBarcodeScannedListener(onBarcodeScanned: (String) -> Unit) {
        // Implement listener functionality
    }
}