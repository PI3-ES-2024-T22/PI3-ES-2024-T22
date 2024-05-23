package com.example.pi3_es_2024_t22

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity

class ReleaseLocationActivity : AppCompatActivity() {

    private lateinit var textViewScannedData: TextView
    private lateinit var btnTakePhoto1: Button
    private lateinit var btnTakePhoto2: Button
    private lateinit var finishRelease: Button
    private lateinit var imageViewPhoto1: ImageView
    private lateinit var imageViewPhoto2: ImageView

    private val CAMERA_PERMISSION_REQUEST_CODE = 1001
    private val CAMERA_REQUEST_CODE_1 = 1002
    private val CAMERA_REQUEST_CODE_2 = 1003

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_release_location)

        // Get the scanned data from the intent
        val scannedData = intent.getStringExtra("scannedData")
        Log.d("ReleaseLocationActivity", "Received scannedData: $scannedData")

        // Display the scanned data
        textViewScannedData = findViewById(R.id.textViewScannedData)
        textViewScannedData.text = scannedData ?: "No data received"

        // Handle RadioGroup selection and button click
        val radioGroup: RadioGroup = findViewById(R.id.radioGroup)
        val btnConfirm: Button = findViewById(R.id.btnConfirm)
        btnTakePhoto1 = findViewById(R.id.btnTakePhoto1)
        btnTakePhoto2 = findViewById(R.id.btnTakePhoto2)
        // finishRelease = findViewById(R.id.finishRelease)
        imageViewPhoto1 = findViewById(R.id.imageViewPhoto1)
        imageViewPhoto2 = findViewById(R.id.imageViewPhoto2)

        btnConfirm.setOnClickListener {
            val selectedOptionId = radioGroup.checkedRadioButtonId
            if (selectedOptionId != -1) {
                val selectedRadioButton: RadioButton = findViewById(selectedOptionId)
                val numberOfPeople = selectedRadioButton.text.toString()
                handleSelection(numberOfPeople, scannedData)
            } else {
                textViewScannedData.text = "Please select an option"
            }
        }

        btnTakePhoto1.setOnClickListener {
            checkCameraPermissionAndOpenCamera(CAMERA_REQUEST_CODE_1)
        }

        btnTakePhoto2.setOnClickListener {
            checkCameraPermissionAndOpenCamera(CAMERA_REQUEST_CODE_2)
        }
    }

    private fun handleSelection(numberOfPeople: String, scannedData: String?) {
        val message = "Number of People: $numberOfPeople\nScanned Data: $scannedData"
        Log.d("ReleaseLocationActivity", message)

        textViewScannedData.text = message

        when (numberOfPeople) {
            "1 Pessoa" -> {
                btnTakePhoto1.visibility = Button.VISIBLE
                btnTakePhoto2.visibility = Button.GONE
                imageViewPhoto1.visibility = ImageView.GONE
                imageViewPhoto2.visibility = ImageView.GONE
            }
            "2 Pessoas" -> {
                btnTakePhoto1.visibility = Button.VISIBLE
                btnTakePhoto2.visibility = Button.VISIBLE
                imageViewPhoto1.visibility = ImageView.GONE
                imageViewPhoto2.visibility = ImageView.GONE
            }
            else -> {
                btnTakePhoto1.visibility = Button.GONE
                btnTakePhoto2.visibility = Button.GONE
                imageViewPhoto1.visibility = ImageView.GONE
                imageViewPhoto2.visibility = ImageView.GONE
            }
        }
    }

    private fun checkCameraPermissionAndOpenCamera(requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            openCamera(requestCode)
        }
    }

    private fun openCamera(requestCode: Int) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, requestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (btnTakePhoto1.visibility == Button.VISIBLE) openCamera(CAMERA_REQUEST_CODE_1)
                if (btnTakePhoto2.visibility == Button.VISIBLE) openCamera(CAMERA_REQUEST_CODE_2)
            } else {
                textViewScannedData.text = "Camera permission denied"
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE_1 -> handlePhotoResult(data, imageViewPhoto1, btnTakePhoto1)
                CAMERA_REQUEST_CODE_2 -> handlePhotoResult(data, imageViewPhoto2, btnTakePhoto2)
            }
        }
    }

    private fun handlePhotoResult(data: Intent?, imageView: ImageView, button: Button) {
        val imageBitmap = data?.extras?.get("data") as Bitmap
        imageView.setImageBitmap(imageBitmap)
        button.visibility = Button.GONE
        imageView.visibility = ImageView.VISIBLE
    }
}