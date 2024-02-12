package tech.resilientgym.resilient
// In commonMain
expect class BarcodeScanner {
    fun startCamera()
    fun stopCamera()
    fun setBarcodeScannedListener(onBarcodeScanned: (String) -> Unit)
    // Other common functionalities
}

