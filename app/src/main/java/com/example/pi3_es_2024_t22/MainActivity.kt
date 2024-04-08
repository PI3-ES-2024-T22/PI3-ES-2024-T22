package com.example.pi3_es_2024_t22

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var buttonLogout: Button
    private lateinit var btn_cad_cartao: TextView
    private lateinit var btn_continue: Button
    private lateinit var viewLockersButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        buttonLogout = findViewById(R.id.btn_logout)
        btn_cad_cartao = findViewById(R.id.btn_cad_cartao)
        btn_continue = findViewById(R.id.btn_prosseguir)
        viewLockersButton = findViewById(R.id.viewLockersButton)

        buttonLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
            finish()
        }

        btn_cad_cartao.setOnClickListener {
            val intent = Intent(applicationContext, RegisterCreditCard::class.java)
            startActivity(intent)
            finish()
        }

        viewLockersButton.setOnClickListener {
            val intent = Intent(applicationContext, MapsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}