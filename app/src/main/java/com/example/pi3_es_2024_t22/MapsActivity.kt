package com.example.pi3_es_2024_t22

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.pi3_es_2024_t22.databinding.ActivityMapsBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var buttonloc: Button
    private lateinit var mDialog: mDialog
    private lateinit var firebaseFirestore: FirebaseFirestore
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buttonloc = findViewById(R.id.btnloc)
        mDialog = mDialog(this)
        firebaseFirestore = FirebaseFirestore.getInstance()

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    buttonloc.setOnClickListener {
        if (currentHour >= 7 && currentHour < 18) {
            firebaseFirestore.collection("estabelecimentos") //COMO PEGAR INFORMAÇÕES DO LOCAL DO MAPA? 1- CRIAR LOCAL MANUALMENTE SE BASEANDO NA PUC ( FACIL ) / 2 - CRIAR BASEADO EM QUALQUER LOCAL DO MAPA ( DIFICIL )
                .document("id_do_estabelecimento")
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val prices = document.data
                        // Exibe os botões de opção de locação no popup com os preços recuperados
                        mDialog.setContentView(R.layout.popup)
                        setupPopup(prices)
                        mDialog.show()
                    } else {
                        Toast.makeText(this, "Erro ao recuperar os preços", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Erro ao recuperar os preços: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Locação disponível apenas entre 7h e 18h", Toast.LENGTH_SHORT).show()
        }
    }
}

