package com.example.pi3_es_2024_t22

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.pi3_es_2024_t22.databinding.ActivityMapsBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import android.widget.TextView

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var buttonloc: Button
    private lateinit var firebaseFirestore: FirebaseFirestore
    private val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        buttonloc = findViewById(R.id.btnloc)
        firebaseFirestore = FirebaseFirestore.getInstance()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        buttonloc.setOnClickListener {
            if (currentHour >= 7 && currentHour < 18) {

                val nomeLocal = "Poco Loco"
                val enderecoLocal = "Av. Profa. Ana Maria Silvestre Adade, 255-395 - Parque das Universidades, Campinas - SP, 13086-130"
                val preco30Min = "30.00"
                val preco1Hora = "50.00"
                val preco2Horas = "100.00"
                val preco4Horas = "150.00"
                val precoAte18 = "300.00"

                val pocolocoLocation = LatLng(-22.835436, -47.0544414)

                val local = hashMapOf(
                    "nome" to nomeLocal,
                    "endereco" to enderecoLocal,
                    "preco_30_min" to preco30Min,
                    "preco_1_hora" to preco1Hora,
                    "preco_2_horas" to preco2Horas,
                    "preco_4_horas" to preco4Horas,
                    "preco_ate_18" to precoAte18,
                    "latitude" to pocolocoLocation.latitude.toString(),
                    "longitude" to pocolocoLocation.longitude.toString()
                )

                showLocationInfoPopup()

                firebaseFirestore.collection("locais")
                    .add(local)
                    .addOnSuccessListener { documentReference ->
                        Toast.makeText(
                            this,
                            "Informações do local adicionadas com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(
                            this,
                            "Erro ao adicionar informações do local: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(this, "Locação disponível apenas entre 7h e 18h", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val pocolocoLocation = LatLng(-22.835436, -47.0544414)

        mMap.addMarker(MarkerOptions().position(pocolocoLocation).title("Poco Loco Bar"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pocolocoLocation, 14f))
    }

    private fun showLocationInfoPopup() {
        val dialogView = layoutInflater.inflate(R.layout.location_info_popup, null)

        val textViewLocationName = dialogView.findViewById<TextView>(R.id.locationName)
        val textViewLocationAddress = dialogView.findViewById<TextView>(R.id.locationAddress)
        val textViewLocationPrices = dialogView.findViewById<TextView>(R.id.locationPrices)
        val buttonConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)

        // Aqui você pode recuperar as informações do Firestore e preencher os TextViews
        // Exemplo:
        firebaseFirestore.collection("locais")
            .document("seu_documento_de_local")
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val nomeLocal = document.getString("nome")
                    val enderecoLocal = document.getString("endereco")
                    val preco30Min = document.getString("preco_30_min")
                    val preco1Hora = document.getString("preco_1_hora")
                    val preco2Horas = document.getString("preco_2_horas")
                    val preco4Horas = document.getString("preco_4_horas")
                    val precoAte18 = document.getString("preco_ate_18")

                    // Aqui você pode montar a string com os preços
                    val pricesText = "Preços:\n30min = R$ $preco30Min\n1h = R$ $preco1Hora\n2h = R$ $preco2Horas\n4h = R$ $preco4Horas\nAté as 18h = R$ $precoAte18"

                    textViewLocationName.text = nomeLocal
                    textViewLocationAddress.text = enderecoLocal
                    textViewLocationPrices.text = pricesText
                } else {
                    Toast.makeText(this, "Erro ao recuperar informações do local", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Erro ao recuperar informações do local: ${exception.message}", Toast.LENGTH_SHORT).show()
            }

        val alertDialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Informações do Local")
            .setCancelable(true)

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()

        buttonConfirm.setOnClickListener {
            // Lógica para confirmar a compra
            alertDialog.dismiss() // Fechar o popup após a confirmação
        }
    }
}

//NAO DEU FETCH CORRETAMENTE MAS O POPUP FUNCIONOU