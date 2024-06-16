package com.example.pi3_es_2024_t22

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.pi3_es_2024_t22.databinding.ActivityMapsBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.util.Calendar

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


    // data class para representar os dados de um marcador (armario)
    data class MarkerData(
        val latLng: GeoPoint = GeoPoint(0.0, 0.0),
        val nome: String = "",
        val info: Map<String, String> = mapOf(),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // instancias do firestore e do firebase auth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()


        // checkar e pedir permissoes ao usuario para acessar a localizacao
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
        // checar e pedir permissoes ao usuario para acessar a localizacao
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
        // habilitacao da localizacao do usuario apos requisicao pelas permissoes
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
                // Logar erro caso nao seja possivel pegar a localizacao do usuario
                Log.e("Location", "Error getting last known location: ", exception)
            }

        // requisacao dos marcadores (armarios) do firestore e criacao dos marcadores no mapa
        firestore.collection("locais").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val markerData = document.toObject(MarkerData::class.java)
                    val position = LatLng(markerData.latLng.latitude, markerData.latLng.longitude)
                    val marker = mMap.addMarker(MarkerOptions().position(position).title(markerData.nome))
                    val infoWithId = markerData.info.toMutableMap()
                    infoWithId["id"] = document.id
                    marker?.tag = infoWithId
                }
            }
            // Logar erro caso nao seja possivel pegar os marcadores do firestore
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents: ", exception)
            }

        // ativando os listeners para os marcadores e para o mapa
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(this)

        // inicializacao para os botoes de navegacao e de informacoes
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
        // ao clicar no botao de alugar armario, abrir dialogo com informacoes do armario
        lockerDialogButton.setOnClickListener {
            selectedMarker?.let { marker ->
                val info = marker.tag as? Map<String, String> ?: mapOf()
                showMarkerInfoDialog(marker.title ?: "Sem nome", info)
            }
        }
        // ao clicar no botao de informacoes, abrir dialogo com informacoes do armario sem a opcao de alugar
        infoOnlyButton.setOnClickListener {
            selectedMarker?.let { marker ->
                val info = marker.tag as? Map<String, String> ?: mapOf()
                showInfoOnlyDialog(info)
            }
        }
    }


    // sobreescrita da funcao ao clicker em um marcador
    override fun onMarkerClick(marker: Marker): Boolean {

        if (isWithinRentalHours()) {
            navigateButton.visibility = FloatingActionButton.VISIBLE
            selectedMarker = marker
        } else {
            // Caso contrário, desabilite o botão de locação
            lockerDialogButton.visibility = FloatingActionButton.GONE
            infoOnlyButton.visibility = FloatingActionButton.GONE
            Toast.makeText(this, "Disponível das 7h às 17h", Toast.LENGTH_LONG).show()
        }

        selectedMarker?.let { marker ->
            val distance = userLocation.distanceTo(Location("Marker").apply {
                latitude = marker.position.latitude
                longitude = marker.position.longitude
            })

            val userUid = auth.currentUser?.uid
            if (userUid != null) {
                // checando se o usuario atual possui cartao cadastrado
                firestore.collection("Pessoas").document(userUid).get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val hasCreditCard = documentSnapshot.get("Cartao") ?: ""
                            if (hasCreditCard !== "" && distance <= 10000) {
                                // Mostrar botao para alugar armario caso o usuario tenha um cartao cadastrado
                                lockerDialogButton.visibility = FloatingActionButton.VISIBLE
                                infoOnlyButton.visibility = FloatingActionButton.GONE
                            } else {
                                // Esconder botao para alugar armario caso o usuario nao tenha um cartao cadastrado
                                lockerDialogButton.visibility = FloatingActionButton.GONE
                                infoOnlyButton.visibility = FloatingActionButton.VISIBLE
                            }
                        } else {
                            // Log de erro caso nao seja possivel pegar o documento do usuario
                            Log.e("UserDocument", "User document does not exist")
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Log de erro
                        Log.e("Firestore", "Error getting user document: ", exception)
                    }
            } else {
                // Esconder botao para alugar armario caso o usuario nao esteja logado
                lockerDialogButton.visibility = FloatingActionButton.GONE
                infoOnlyButton.visibility = FloatingActionButton.VISIBLE
            }
        }
        return true
    }

    // sobreescrita da funcao ao clickar no mapa
    override fun onMapClick(p0: LatLng) {
        // esconder botoes ao clicar no mapa
        navigateButton.visibility = FloatingActionButton.GONE
        lockerDialogButton.visibility = FloatingActionButton.GONE
        infoOnlyButton.visibility = FloatingActionButton.GONE
        selectedMarker = null
    }

    // funcao para mostrar o dialogo que contem informacoes e opcao de alugar armario
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

        // definicao dos textos de acordo com os precos cadastrados no firebase

        dialogTitle.text = "Armário - ${info["referencePoint"]}"
        locationAddress.text = "Endereço - ${info["address"]}"

        radioThirtyMinutes.text = "30 Minutos: R$ ${info["thirtyMinutesPrice"]}"
        radioOneHour.text = "1 Hora: R$ ${info["oneHourPrice"]}"
        radioTwoHours.text = "2 Horas: R$ ${info["twoHoursPrice"]}"
        radioFourHours.text = "4 Horas: R$ ${info["fourHoursPrice"]}"
        radioNowUntilSix.text = "Agora até as 18h: R$ ${info["nowUntilSixPrice"]}"


        // utilizando o horario atual do usuario para desabilitar o botao de alugar armario
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        when {
            currentHour in 17..18 -> {
                radioThirtyMinutes.isEnabled = true
                radioOneHour.isEnabled = true
                radioTwoHours.isEnabled = false
                radioFourHours.isEnabled = false
                radioNowUntilSix.isEnabled = false
            }
            currentHour in 16..17 -> {
                radioThirtyMinutes.isEnabled = true
                radioOneHour.isEnabled = true
                radioFourHours.isEnabled = false
                radioNowUntilSix.isEnabled = false
            }
            currentHour in 7..18 -> {
                radioThirtyMinutes.isEnabled = true
                radioOneHour.isEnabled = true
                radioTwoHours.isEnabled = true
                radioFourHours.isEnabled = true
                radioNowUntilSix.isEnabled = true
            }
            else -> {
                radioThirtyMinutes.isEnabled = false
                radioOneHour.isEnabled = false
                radioTwoHours.isEnabled = false
                radioFourHours.isEnabled = false
                radioNowUntilSix.isEnabled = false
            }
        }

        radioTwoHours.invalidate()
        radioFourHours.invalidate()
        radioNowUntilSix.invalidate()

        if (currentHour in 7..8) {
            radioNowUntilSix.isEnabled = true
        } else {
            radioNowUntilSix.isEnabled = false
        }

        val submitButton = dialogView.findViewById<Button>(R.id.submitButton)
        val qrCodeImageView = dialogView.findViewById<ImageView>(R.id.qrCodeImageView)
        var createdDocumentId = ""

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

            var hours = 0.0

            if (selectedPrice == info["thirtyMinutesPrice"]) {
                hours = 0.5
            } else if (selectedPrice == info["oneHourPrice"]) {
                hours = 1.0
            } else if (selectedPrice == info["twoHoursPrice"]) {
                hours = 2.0
            } else if (selectedPrice == info["fourHoursPrice"]) {
                hours = 4.0
            } else if (selectedPrice == info["nowUntilSixPrice"]) {
                hours = 0.0
            }

            if (selectedPrice != null) {
                // Criar documento na coleção "locacoes"
                val locacoesRef = firestore.collection("locacoes")
                val userUid = auth.currentUser?.uid
                val markerId = info["id"]

                if (userUid != null && markerId != null) {
                    val locacaoData = hashMapOf(
                        "usuarioId" to userUid,
                        "localId" to markerId,
                        "preco" to selectedPrice,
                        "ativo" to false,
                        "data" to Calendar.getInstance().time.toString(),
                        "horas" to hours
                    )

                    locacoesRef.add(locacaoData)
                        .addOnSuccessListener { documentReference ->
                            createdDocumentId = documentReference.id

                            // Exibir o QR code e esconder os elementos de entrada de preço

                            val qrCodeBitmap = generateQRCode(createdDocumentId)
                            qrCodeImageView.setImageBitmap(qrCodeBitmap)
                            qrCodeImageView.visibility = View.VISIBLE

                            priceRadioGroup.visibility = View.GONE
                            submitButton.visibility = View.GONE

                            val params = qrCodeImageView.layoutParams
                            params.width = 800
                            params.height = 800
                            qrCodeImageView.layoutParams = params
                        }
                        .addOnFailureListener { e ->
                            // Log de erro
                            Log.e("Firestore", "Error adding document", e)
                        }
                }
            } else {
                Log.e("SelectedPrice", "No price selected")
            }
        }


        // builder responsavel por mostrar o dialogo com o qr code
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        builder.setNegativeButton("Cancelar") { dialog, which ->
            val documentId = createdDocumentId
            if (documentId != null) {
                removeLocacaoDocument(documentId)
            }
            dialog.dismiss()
        }

        builder.show()
    }

    // funcao para remover um documento da colecao "locacoes" no firestore caso o usuario cancele a locacao
    private fun removeLocacaoDocument(documentId: String) {
        firestore.collection("locacoes").document(documentId)
            .delete()
            .addOnSuccessListener {
                // Log de sucesso
                Log.i("Firebase", "Document deleted with ID: $documentId")
            }
            .addOnFailureListener { e ->
                // Log de erro
                Log.w("Firebase", "Error deleting document: $e")
            }
    }


    // funcao de geracao do qr code
    private fun generateQRCode(id: String): Bitmap {
        val gson = Gson()
        val charset = Charsets.UTF_8
        val byteArray = id.toByteArray(charset)

        val hints = mapOf<EncodeHintType, ErrorCorrectionLevel>(Pair(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H))
        val matrix = MultiFormatWriter().encode(String(byteArray, charset), BarcodeFormat.QR_CODE, 300, 300, hints)
        val barcodeEncoder = BarcodeEncoder()
        return barcodeEncoder.createBitmap(matrix)
    }

    // funcao para mostrar o dialogo que contem apenas informacoes basicas sobre o armario
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

    // Sobrescrita da função onBackPressed para retornar à tela de login ao pressionar o botão de voltar
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

    private fun isWithinRentalHours(): Boolean {
        return true
    }
}