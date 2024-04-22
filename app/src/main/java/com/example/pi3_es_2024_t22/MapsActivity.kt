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
import com.google.firebase.auth.FirebaseAuth
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
    private lateinit var infoOnlyButton: FloatingActionButton
    private var selectedMarker: Marker? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userLocation: Location
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    private lateinit var auth: FirebaseAuth



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
        auth = FirebaseAuth.getInstance()

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
                // Pegar a ultima localizacao do usuario
                if (location != null) {
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
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->

                if (location != null) {
                    userLocation = location

                    // Colocando o foco do mapa na localizacao atual do usuario
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Location", "Error getting last known location: ", exception)
            }

        firestore.collection("locais").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val markerData = document.toObject(MarkerData::class.java)
                    val position = LatLng(markerData.latLng.latitude, markerData.latLng.longitude)
                    val marker = mMap.addMarker(MarkerOptions().position(position).title(markerData.nome))
                    marker?.tag = markerData.info // Salvando as informacoes do armario no marker
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
        infoOnlyButton = findViewById(R.id.openInfoOnlyDialogButton)
        navigateButton.setOnClickListener {
            selectedMarker?.let { marker ->
                // Abrir o google maps para navegacao
                val uri = "google.navigation:q=${marker.position.latitude},${marker.position.longitude}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                intent.setPackage("com.google.android.apps.maps")
                startActivity(intent)
            }
        }
        lockerDialogButton.setOnClickListener {
            selectedMarker?.let { marker ->
                val info = marker.tag as? Map<String, String> ?: mapOf()
                showMarkerInfoDialog(marker.title ?: "Sem nome", info)
            }
        }
        infoOnlyButton.setOnClickListener {
            selectedMarker?.let { marker ->
                val info = marker.tag as? Map<String, String> ?: mapOf()
                showInfoOnlyDialog(info)
            }
        }
    }



    override fun onMarkerClick(marker: Marker): Boolean {
        // Checcar se o usuario possui um cartao cadastrado


        navigateButton.visibility = FloatingActionButton.VISIBLE

        selectedMarker = marker
        selectedMarker?.let { marker ->
            val distance = userLocation.distanceTo(Location("Marker").apply {
                latitude = marker.position.latitude
                longitude = marker.position.longitude
            })

            // Verificando distancia do usuario em relacao ao armario
            val userUid = auth.currentUser?.uid
            if (userUid != null) {
                firestore.collection("Pessoas").document(userUid).get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val hasCreditCard = documentSnapshot.get("Cartao") ?: ""
                            if (hasCreditCard !== "" && distance <= 1000) {
                                // Mostrar botao para alugar armario caso o usuario tenha um cartao cadastrado
                                lockerDialogButton.visibility = FloatingActionButton.VISIBLE
                                infoOnlyButton.visibility = FloatingActionButton.GONE
                            } else {
                                // Esconder botao para alugar armario caso o usuario nao tenha um cartao cadastrado
                                lockerDialogButton.visibility = FloatingActionButton.GONE
                                infoOnlyButton.visibility = FloatingActionButton.VISIBLE
                            }
                        } else {
                            Log.e("UserDocument", "User document does not exist")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Firestore", "Error getting user document: ", exception)
                    }
            } else {
                infoOnlyButton.visibility = FloatingActionButton.VISIBLE
                Log.e("User", "User is not logged in")
            }
        }



        return true
    }

    override fun onMapClick(p0: LatLng) {
        navigateButton.visibility = FloatingActionButton.GONE
        lockerDialogButton.visibility = FloatingActionButton.GONE
        infoOnlyButton.visibility = FloatingActionButton.GONE
        selectedMarker = null
    }

    private fun showMarkerInfoDialog(nome: String, info: Map<String, String>) {
        val dialogView = layoutInflater.inflate(R.layout.location_info_dialog, null)


        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val locationAddress = dialogView.findViewById<TextView>(R.id.locationAddress)

        val priceRadioGroup = dialogView.findViewById<RadioGroup>(R.id.priceRadioGroup)
        val radioThirtyMinutes = dialogView.findViewById<RadioButton>(R.id.radioThirtyMinutes)
        val radioOneHour = dialogView.findViewById<RadioButton>(R.id.radioOneHour)
        val radioTwoHours = dialogView.findViewById<RadioButton>(R.id.radioTwoHours)
        val radioFourHours = dialogView.findViewById<RadioButton>(R.id.radioFourHours)
        val radioNowUntilSix = dialogView.findViewById<RadioButton>(R.id.radioNowUntilSix)

        dialogTitle.text = "Armário - ${info["referencePoint"]}"
        locationAddress.text = "Endereço - ${info["address"]}"

        radioThirtyMinutes.text = "30 Minutos: R$ ${info["thirtyMinutesPrice"]}"
        radioOneHour.text = "1 Hora: R$ ${info["oneHourPrice"]}"
        radioTwoHours.text = "2 Horas: R$ ${info["twoHoursPrice"]}"
        radioFourHours.text = "4 Horas: R$ ${info["fourHoursPrice"]}"
        radioNowUntilSix.text = "Agora até as 18h: R$ ${info["nowUntilSixPrice"]}"

        val submitButton = dialogView.findViewById<Button>(R.id.submitButton)
        val qrCodeImageView = dialogView.findViewById<ImageView>(R.id.qrCodeImageView)

        submitButton.setOnClickListener {
            // Pegar a opcao de preco selecionada
            val selectedPrice = when (priceRadioGroup.checkedRadioButtonId) {
                R.id.radioThirtyMinutes -> info["thirtyMinutesPrice"]
                R.id.radioOneHour -> info["oneHourPrice"]
                R.id.radioTwoHours -> info["twoHoursPrice"]
                R.id.radioFourHours -> info["fourHoursPrice"]
                R.id.radioNowUntilSix -> info["nowUntilSixPrice"]
                else -> null
            }

            // Geracao do qr code
            if (selectedPrice != null) {
                val qrCodeBitmap = generateQRCode(selectedPrice)
                qrCodeImageView.setImageBitmap(qrCodeBitmap)
                qrCodeImageView.visibility = View.VISIBLE

                priceRadioGroup.visibility = View.GONE
                submitButton.visibility = View.GONE

                val params = qrCodeImageView.layoutParams
                params.width = 800
                params.height = 800
                qrCodeImageView.layoutParams = params
            } else {
                Log.e("SelectedPrice", "No price selected")
            }
        }

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun generateQRCode(price: String): Bitmap {
        val charset = Charsets.UTF_8
        val byteArray = price.toByteArray(charset)

        val hints = mapOf<EncodeHintType, ErrorCorrectionLevel>(Pair(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H))
        val matrix = MultiFormatWriter().encode(String(byteArray, charset), BarcodeFormat.QR_CODE, 300, 300, hints)
        val barcodeEncoder = BarcodeEncoder()
        return barcodeEncoder.createBitmap(matrix)
    }

    private fun showInfoOnlyDialog(info: Map<String, String>) {
        val dialogView = layoutInflater.inflate(R.layout.info_only_dialog, null)
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val dialogAddress = dialogView.findViewById<TextView>(R.id.locationAddress)

        dialogTitle.text = "Ponto de referencia - ${info["referencePoint"]}"
        dialogAddress.text = "Endereço - ${info["address"]}"

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        if (auth.currentUser == null) {
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }
        finish()
    }
}