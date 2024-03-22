package com.example.pi3_es_2024_t22

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class Login : AppCompatActivity() {
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var buttonLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var buttonRegister: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var buttonForgotPassword: TextView

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val intent = Intent(this@Login, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        buttonLogin = findViewById(R.id.btn_login)
        progressBar = findViewById(R.id.progressBar)
        buttonRegister = findViewById(R.id.registerNow)
        buttonForgotPassword = findViewById(R.id.forgotPass)

        auth = FirebaseAuth.getInstance()

        buttonRegister.setOnClickListener {
            val intent = Intent(this@Login, Register::class.java)
            startActivity(intent)
            finish()
        }

        buttonForgotPassword.setOnClickListener {
            val intent = Intent(this@Login, ForgotPassword::class.java)
            startActivity(intent)
            finish()
        }

        buttonLogin.setOnClickListener {
            progressBar.visibility = View.VISIBLE

            val email: String = editTextEmail.text.toString()
            val password: String = editTextPassword.text.toString()

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this@Login, "Entre com o seu Email", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this@Login, "Entre com sua Senha", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.let {
                            val userId = it.uid
                            val intent = Intent(this@Login, MainActivity::class.java)
                            intent.putExtra("userId", userId)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(
                            baseContext,
                            "Login Falhou.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}

//VERIFICACAO SE O USUARIO EH CLIENTE OU GERENTE AO APERTAR O BOTAO DE LOGIN

//package com.example.pi3_es_2024_t22
//
//import android.content.Intent
//import android.os.Bundle
//import android.text.TextUtils
//import android.view.View
//import android.widget.Button
//import android.widget.ProgressBar
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.material.textfield.TextInputEditText
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//import com.google.firebase.database.DatabaseReference
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.database.ValueEventListener
//
//class Login : AppCompatActivity() {
//    private lateinit var editTextEmail: TextInputEditText
//    private lateinit var editTextPassword: TextInputEditText
//    private lateinit var buttonLogin: Button
//    private lateinit var progressBar: ProgressBar
//    private lateinit var buttonRegister: TextView
//    private lateinit var auth: FirebaseAuth
//    private lateinit var buttonForgotPassword: TextView
//    private lateinit var database: DatabaseReference
//
//
//    public override fun onStart() {
//        super.onStart()
//        val currentUser = auth.currentUser
//
//        if (currentUser != null) {
//            val intent = Intent(this@Login, MainActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_login)
//
//        editTextEmail = findViewById(R.id.email)
//        editTextPassword = findViewById(R.id.password)
//        buttonLogin = findViewById(R.id.btn_login)
//        progressBar = findViewById(R.id.progressBar)
//        buttonRegister = findViewById(R.id.registerNow)
//        buttonForgotPassword = findViewById(R.id.forgotPass)
//
//        auth = FirebaseAuth.getInstance()
//
//        buttonRegister.setOnClickListener {
//            val intent = Intent(this@Login, Register::class.java)
//            startActivity(intent)
//            finish()
//        }
//
//        buttonForgotPassword.setOnClickListener {
//            val intent = Intent(this@Login, ForgotPassword::class.java)
//            startActivity(intent)
//            finish()
//        }
//
//        buttonLogin.setOnClickListener {
//            progressBar.visibility = View.VISIBLE
//
//            val email: String = editTextEmail.text.toString()
//            val password: String = editTextPassword.text.toString()
//
//            if (TextUtils.isEmpty(email)) {
//                Toast.makeText(this@Login, "Entre com o seu Email", Toast.LENGTH_SHORT).show()
//                progressBar.visibility = View.GONE
//                return@setOnClickListener
//            }
//
//            if (TextUtils.isEmpty(password)) {
//                Toast.makeText(this@Login, "Entre com sua Senha", Toast.LENGTH_SHORT).show()
//                progressBar.visibility = View.GONE
//                return@setOnClickListener
//            }
//
//            auth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this) { task ->
//                    database = FirebaseDatabase.getInstance().getReference("Pessoas")
//
//                    progressBar.visibility = View.GONE
//                    if (task.isSuccessful) {
//                        val user = auth.currentUser
//                        user?.let {
//                            val userId = it.uid
//                            val userRef = database.child(userId)
//
//                            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
//                                override fun onDataChange(snapshot: DataSnapshot) {
//                                    val perfil = snapshot.child("Perfil").getValue(String::class.java)
//
//                                    // Verifica o tipo de usuário e direciona para a tela correspondente
//                                    if (perfil == "Gerente") {
//                                        val intent = Intent(this@Login, TelaGerente::class.java)
//                                        startActivity(intent)
//                                    } else if (perfil == "Cliente") {
//                                        val intent = Intent(this@Login, TelaCliente::class.java)
//                                        startActivity(intent)
//                                    } else {
//                                        Toast.makeText(
//                                            baseContext,
//                                            "Tipo de usuário inválido.",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    }
//                                }
//
//                                override fun onCancelled(error: DatabaseError) {
//                                    Toast.makeText(
//                                        baseContext,
//                                        "Erro ao acessar o banco de dados.",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                }
//                            })
//                        }
//                    } else {
//                        Toast.makeText(
//                            baseContext,
//                            "Login falhou.",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//
//        }
//    }
//}
