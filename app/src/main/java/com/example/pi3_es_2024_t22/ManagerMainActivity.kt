package com.example.pi3_es_2024_t22

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

class ManagerMainActivity : AppCompatActivity() {
    private lateinit var btnScanQR: Button
    private lateinit var txtScanned: TextView
    private lateinit var scannedLocation: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manager_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnScanQR = findViewById(R.id.btnScanQR)
        txtScanned = findViewById(R.id.txtScanned)
        val gmsScannerOptions = configureScannerOption()
        val instance = getBarcodeScannerInstance(gmsScannerOptions)

        btnScanQR.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                // Request camera permission
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE)
            } else {
                // Permission already granted
                initiateScanner(
                    instance,
                    onSuccess = { barcode ->
                        scannedLocation = barcode.displayValue.toString()
                        renderLocationRelease()
                    },
                    onCancel = {
                        txtScanned.text = "Canceled"
                    },
                    onFailure = { e ->
                        txtScanned.text = "Error: ${e.message}"
                    }
                )
            }
        }
    }

    private fun renderLocationRelease() {
        val intent = Intent(this, ReleaseLocationActivity::class.java)
        intent.putExtra("scannedData", scannedLocation)
        startActivity(intent)
        Log.d(TAG, "renderLocationRelease: Starting ReleaseLocationActivity with scannedData: $scannedLocation")
    }

    private fun configureScannerOption(): GmsBarcodeScannerOptions {
        return GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC
            )
            .build()
    }

    private fun getBarcodeScannerInstance(gmsBarcodeScannerOptions: GmsBarcodeScannerOptions): GmsBarcodeScanner {
        return GmsBarcodeScanning.getClient(this, gmsBarcodeScannerOptions)
    }

    private fun initiateScanner(
        gmsBarcodeScanner: GmsBarcodeScanner,
        onSuccess: (Barcode) -> Unit,
        onCancel: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        gmsBarcodeScanner.startScan()
            .addOnSuccessListener { barcode ->
                val result = barcode.rawValue
                Log.d(TAG, "initiateScanner: $result")
                when (barcode.valueType) {
                    Barcode.TYPE_URL -> {
                        Log.d(TAG, "initiateScanner: ${barcode.valueType}")
                    }
                    else -> {
                        Log.d(TAG, "initiateScanner: ${barcode.valueType}")
                    }
                }
                Log.d(TAG, "initiateScanner: Display value: ${barcode.displayValue}")
                Log.d(TAG, "initiateScanner: Format: ${barcode.format}")
                onSuccess(barcode)
            }
            .addOnCanceledListener {
                onCancel()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, proceed with scanning
                val gmsScannerOptions = configureScannerOption()
                val instance = getBarcodeScannerInstance(gmsScannerOptions)
                initiateScanner(
                    instance,
                    onSuccess = { barcode ->
                        txtScanned.text = barcode.displayValue
                    },
                    onCancel = {
                        txtScanned.text = "Canceled"
                    },
                    onFailure = { e ->
                        txtScanned.text = "Error: ${e.message}"
                    }
                )
            } else {
                // Permission denied, handle appropriately
                txtScanned.text = "Camera permission denied"
            }
        }
    }
}
