package com.example.pi3_es_2024_t22

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var buttonLogout: Button
    private lateinit var textView: TextView
    private lateinit var btn_cad_cartao: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        buttonLogout = findViewById(R.id.btn_logout)
        btn_cad_cartao = findViewById(R.id.btn_cad_cartao)

        buttonLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
            finish()
        }

        btn_cad_cartao.setOnClickListener {
            val intent = Intent(applicationContext, CadastrarCartao::class.java)
            startActivity(intent)
            finish()

            //if () --> SE O CARTAO ESTIVER CADASTRADO --> LIBERA OUTRAS FUNCOES DO APLICATIVO PARA O USUARIO
        }
    }


    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
            finish()
        }
    }
}
