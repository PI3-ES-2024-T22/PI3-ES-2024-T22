package com.example.pi3_es_2024_t22 // Define o pacote da classe

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

class Login : AppCompatActivity() {
    private lateinit var editTextEmail: TextInputEditText // Variável para o campo de email
    private lateinit var editTextPassword: TextInputEditText // Variável para o campo de senha
    private lateinit var buttonLogin: Button // Variável para o botão de login
    private lateinit var progressBar: ProgressBar // Variável para a barra de progresso
    private lateinit var textView: TextView // Variável para o texto de registro agora
    private lateinit var auth: FirebaseAuth // Variável para a instância do FirebaseAuth

    public override fun onStart() { //Funcao para verificar se o usuario ja esta autenticado
        super.onStart()
        // Verifica se o usuário já está autenticado (não nulo) e atualiza a interface do usuário
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

        // Inicializa os elementos da interface do usuário
        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        buttonLogin = findViewById(R.id.btn_login)
        progressBar = findViewById(R.id.progressBar)
        textView = findViewById(R.id.registerNow)

        auth = FirebaseAuth.getInstance() // Inicializa a instância do FirebaseAuth

        textView.setOnClickListener { // Define o comportamento do texto de registro agora quando clicado
            val intent = Intent(this@Login, Register::class.java)
            startActivity(intent) // Inicia a atividade de registro
            finish() // Finaliza a atividade atual (login)
        }

        buttonLogin.setOnClickListener { // Define o comportamento do botão de login quando clicado
            progressBar.visibility =
                View.VISIBLE // Torna a barra de progresso visível ao clicar no botão de login

            val email: String = editTextEmail.text.toString() // Obtém o email inserido pelo usuário
            val password: String =
                editTextPassword.text.toString() // Obtém a senha inserida pelo usuário

            // Validação dos campos de email e senha
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this@Login, "Enter email", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this@Login, "Enter password", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }

            // Realiza a autenticação do usuário com email e senha fornecidos
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility = View.GONE // Oculta a barra de progresso
                    if (task.isSuccessful) {
                        // Login bem-sucedido, atualiza a interface do usuário com as informações do usuário autenticado
                        Log.d("LoginActivity", "signInWithEmail:success")
                        val intent = Intent(this@Login, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Se a tentativa de login falhar, exibe uma mensagem de erro
                        Log.w("LoginActivity", "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Login failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}