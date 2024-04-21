package com.example.pi3_es_2024_t22

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.pi3_es_2024_t22.databinding.ActivityMapsBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.Marker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.journeyapps.barcodescanner.BarcodeEncoder

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var navigateButton: FloatingActionButton
    private lateinit var lockerDialogButton: FloatingActionButton
    private var selectedMarker: Marker? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userLocation: Location
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    data class MarkerData(
        val latLng: GeoPoint = GeoPoint(0.0, 0.0),
        val nome: String = "",
        val info: Map<String, String> = mapOf(),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // Save user's current location
                    userLocation = location
                }
            }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                1
            )
            return
        }
        mMap.isMyLocationEnabled = true
        firestore.collection("locais").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val markerData = document.toObject(MarkerData::class.java)
                    val position = LatLng(markerData.latLng.latitude, markerData.latLng.longitude)
                    val marker = mMap.addMarker(MarkerOptions().position(position).title(markerData.nome))
                    marker?.tag = markerData.info // Store the info map in the marker tag
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents: ", exception)
            }
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(this)

        // Initialize button
        navigateButton = findViewById(R.id.navigateButton)
        lockerDialogButton = findViewById(R.id.openLockerDialogButton)
        navigateButton.setOnClickListener {
            selectedMarker?.let { marker ->
                // Start navigation to the clicked marker
                val uri = "google.navigation:q=${marker.position.latitude},${marker.position.longitude}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                intent.setPackage("com.google.android.apps.maps")
                startActivity(intent)
            }
        }
        lockerDialogButton.setOnClickListener {
            selectedMarker?.let { marker ->
                // Display dialog with marker nome and info
                val info = marker.tag as? Map<String, String> ?: mapOf() // Retrieve info from marker tag
                showMarkerInfoDialog(marker.title ?: "Sem nome", info)
            }
        }
    }


    override fun onMarkerClick(marker: Marker): Boolean {
        // Show button when marker is clicked
        navigateButton.visibility = FloatingActionButton.VISIBLE
        selectedMarker = marker
        selectedMarker?.let { marker ->
            val distance = userLocation.distanceTo(Location("Marker").apply {
                latitude = marker.position.latitude
                longitude = marker.position.longitude
            })

            // Show or hide the lockerDialogButton based on distance
            if (distance >= 1000) { // 1000 meters = 1 km
                lockerDialogButton.visibility = FloatingActionButton.GONE
            } else {
                lockerDialogButton.visibility = FloatingActionButton.VISIBLE
            }
        }
        // Consume the event to prevent the default behavior (info window display)
        return true
    }

    override fun onMapClick(p0: LatLng) {
        // Hide button when clicking off a marker
        navigateButton.visibility = FloatingActionButton.GONE
        lockerDialogButton.visibility = FloatingActionButton.GONE
        selectedMarker = null
    }

    private fun showMarkerInfoDialog(nome: String, info: Map<String, String>) {
        val dialogView = layoutInflater.inflate(R.layout.location_info_dialog, null)


        // Initialize TextViews for dialog title and address
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val locationAddress = dialogView.findViewById<TextView>(R.id.locationAddress)

        // Initialize RadioGroup and RadioButtons for price options
        val priceRadioGroup = dialogView.findViewById<RadioGroup>(R.id.priceRadioGroup)
        val radioThirtyMinutes = dialogView.findViewById<RadioButton>(R.id.radioThirtyMinutes)
        val radioOneHour = dialogView.findViewById<RadioButton>(R.id.radioOneHour)
        val radioTwoHours = dialogView.findViewById<RadioButton>(R.id.radioTwoHours)
        val radioFourHours = dialogView.findViewById<RadioButton>(R.id.radioFourHours)
        val radioNowUntilSix = dialogView.findViewById<RadioButton>(R.id.radioNowUntilSix)

        // Set dialog title and address
        dialogTitle.text = "Armário - ${info["referencePoint"]}"
        locationAddress.text = "Endereço - ${info["address"]}"

        // Set price options text
        radioThirtyMinutes.text = "30 Minutos: R$ ${info["thirtyMinutesPrice"]}"
        radioOneHour.text = "1 Hora: R$ ${info["oneHourPrice"]}"
        radioTwoHours.text = "2 Horas: R$ ${info["twoHoursPrice"]}"
        radioFourHours.text = "4 Horas: R$ ${info["fourHoursPrice"]}"
        radioNowUntilSix.text = "Agora até as 18h: R$ ${info["nowUntilSixPrice"]}"

        val submitButton = dialogView.findViewById<Button>(R.id.submitButton)
        val qrCodeImageView = dialogView.findViewById<ImageView>(R.id.qrCodeImageView)

        // Set click listener for the Submit Button
        submitButton.setOnClickListener {
            // Get the selected price option
            val selectedPrice = when (priceRadioGroup.checkedRadioButtonId) {
                R.id.radioThirtyMinutes -> info["thirtyMinutesPrice"]
                R.id.radioOneHour -> info["oneHourPrice"]
                R.id.radioTwoHours -> info["twoHoursPrice"]
                R.id.radioFourHours -> info["fourHoursPrice"]
                R.id.radioNowUntilSix -> info["nowUntilSixPrice"]
                else -> null
            }

            // Generate QR code based on the selected price
            if (selectedPrice != null) {
                val qrCodeBitmap = generateQRCode(selectedPrice)
                qrCodeImageView.setImageBitmap(qrCodeBitmap)
                qrCodeImageView.visibility = View.VISIBLE

                // Hide the RadioGroup and Submit Button
                priceRadioGroup.visibility = View.GONE
                submitButton.visibility = View.GONE

                val params = qrCodeImageView.layoutParams
                params.width = 800
                params.height = 800
                qrCodeImageView.layoutParams = params
            } else {
                // Handle case when no price is selected
                Log.e("SelectedPrice", "No price selected")
            }
        }

        // Initialize AlertDialog.Builder
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun generateQRCode(price: String): Bitmap {
        // Convert the price to a byte array
        val charset = Charsets.UTF_8
        val byteArray = price.toByteArray(charset)

        // Generate QR code from the byte array
        val hints = mapOf<EncodeHintType, ErrorCorrectionLevel>(Pair(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H))
        val matrix = MultiFormatWriter().encode(String(byteArray, charset), BarcodeFormat.QR_CODE, 300, 300, hints)
        val barcodeEncoder = BarcodeEncoder()
        return barcodeEncoder.createBitmap(matrix)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}