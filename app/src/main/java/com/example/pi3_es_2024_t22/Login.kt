package com.example.pi3_es_2024_t22

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Login : AppCompatActivity() {
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var buttonLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var buttonRegister: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var buttonForgotPassword: TextView
    private lateinit var database: DatabaseReference
    private lateinit var showLockersButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializando o FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Verifica se há um usuário logado, se sim, direciona para a MainActivity
        val user = auth.currentUser
        if (user !== null) {
            val intent = Intent(this@Login, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Inicializando as views
        editTextEmail = findViewById<TextInputEditText>(R.id.email).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        editTextPassword = findViewById<TextInputEditText>(R.id.password).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        buttonLogin = findViewById(R.id.btn_login)
        progressBar = findViewById(R.id.progressBar)
        buttonRegister = findViewById(R.id.registerNow)
        buttonForgotPassword = findViewById(R.id.forgotPass)
        showLockersButton = findViewById(R.id.showLockersButton)

        // Define o OnClickListener para o botão de registro
        buttonRegister.setOnClickListener {
            val intent = Intent(this@Login, Register::class.java)
            startActivity(intent)
            finish()
        }

        // Define o OnClickListener para o botão de esqueci a senha
        buttonForgotPassword.setOnClickListener {
            val intent = Intent(this@Login, ForgotPassword::class.java)
            startActivity(intent)
            finish()
        }

        // Define o OnClickListener para o botão de mostrar os lockers
        showLockersButton.setOnClickListener {
            val intent = Intent(this@Login, MapsActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Define o OnClickListener para o botão de login
        buttonLogin.setOnClickListener {
            progressBar.visibility = View.VISIBLE

            val email: String = editTextEmail.text.toString()
            val password: String = editTextPassword.text.toString()

            // Verifica se o campo de e-mail está vazio
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this@Login, "Entre com o seu Email", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }

            // Verifica se o campo de senha está vazio
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this@Login, "Entre com sua Senha", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }

            // Autenticação do usuário usando o FirebaseAuth
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility = View.GONE

                    // Verifica se o login foi bem-sucedido
                    if (task.isSuccessful) {
                        val user = auth.currentUser

                        // Verifica se o e-mail do usuário foi verificado
                        if (auth.currentUser?.isEmailVerified == true) {
                            user?.let {
                                val userId = it.uid
                                database = FirebaseDatabase.getInstance().getReference("Pessoas")
                                val userRef = database.child(userId)

                                // Direciona para a MainActivity após o login bem-sucedido
                                val intent = Intent(this@Login, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            // Mensagem para verificar o e-mail do usuário
                            Toast.makeText(
                                this@Login,
                                "Verifique seu email para confirmar o login",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        // Mensagem de falha no login
                        Log.e("LoginActivity", "Falha no login", task.exception)
                        Toast.makeText(
                            this@Login,
                            "Login falhou. Verifique suas credenciais e tente novamente.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}
