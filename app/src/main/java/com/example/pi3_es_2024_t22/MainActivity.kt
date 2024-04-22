package com.example.pi3_es_2024_t22

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var buttonLogout: Button
    private lateinit var btn_cad_cartao: TextView
    private lateinit var btn_continue: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        buttonLogout = findViewById(R.id.btn_logout)
        btn_cad_cartao = findViewById(R.id.btn_cad_cartao)
        btn_continue = findViewById(R.id.btn_prosseguir)

        buttonLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
            finish()
        }

        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("Pessoas").document(userId).get()
                .addOnSuccessListener { documentSnapshot ->
                    val cartao = documentSnapshot.get("Cartao") as? Map<String, Any>
                    if (cartao != null && cartao.isNotEmpty()) {
                        btn_cad_cartao.visibility = TextView.GONE
                    } else {
                        btn_cad_cartao.visibility = TextView.VISIBLE
                    }
                }
        }

        btn_cad_cartao.setOnClickListener {
            val intent = Intent(applicationContext, RegisterCreditCard::class.java)
            startActivity(intent)
            finish()
        }

        btn_continue.setOnClickListener {
            val intent = Intent(applicationContext, MapsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
