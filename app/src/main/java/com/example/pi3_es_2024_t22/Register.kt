package com.example.pi3_es_2024_t22 // Define o pacote da classe

import android.content.Intent // Utilizado para iniciar uma atividade
import android.os.Bundle // Utilizado para passar dados entre componentes Android
import android.text.TextUtils // Biblioteca para manipulação de strings, fornecendo funções extras
import android.view.View // Biblioteca para trabalhar com componentes de interface do usuário
import android.widget.Button // Usado para manipular botões no layout XML
import android.widget.ProgressBar // Usado para exibir uma barra de progresso na interface do usuário
import android.widget.TextView // Usado para manipular texto no layout XML
import android.widget.Toast // Biblioteca para exibir mensagens pop-up
import androidx.appcompat.app.AppCompatActivity // Biblioteca que nos permite usar a classe pai AppCompatActivity()
import com.example.pi3_es_2024_t22.R
import com.google.android.material.textfield.TextInputEditText // Usado para manipular TextInputEditText no layout XML
import com.google.firebase.auth.FirebaseAuth // Biblioteca para autenticação do Firebase

class Register : AppCompatActivity() {
    private lateinit var editTextEmail: TextInputEditText // Variável para o campo de email
    private lateinit var editTextPassword: TextInputEditText // Variável para o campo de senha
    private lateinit var buttonRegister: Button // Variável para o botão de registro
    private lateinit var progressBar: ProgressBar // Variável para a barra de progresso
    private lateinit var textView: TextView // Variável para o texto de login agora
    private lateinit var auth: FirebaseAuth // Variável para a instância do FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Associa as variáveis aos elementos do layout XML
        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        buttonRegister = findViewById(R.id.btn_register)
        progressBar = findViewById(R.id.progressBar)
        textView = findViewById(R.id.loginNow)

        // Inicializa a instância do FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Define o comportamento do texto de login agora quando clicado
        textView.setOnClickListener {
            val intent = Intent(this@Register, Login::class.java)
            startActivity(intent)
            finish()
        }

        // Define o comportamento do botão de registro quando clicado
        buttonRegister.setOnClickListener {
            progressBar.visibility = View.VISIBLE // Torna a barra de progresso visível

            val email: String = editTextEmail.text.toString()
            val password: String = editTextPassword.text.toString()

            // Validação dos campos de email e senha
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this@Register, "Enter email", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this@Register, "Enter password", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }

            // Cria um novo usuário com o email e senha fornecidos
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility = View.GONE // Oculta a barra de progresso
                    if (task.isSuccessful) {
                        // Criação de conta bem-sucedida
                        Toast.makeText(
                            baseContext,
                            "Authentication was successful.",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this@Register, MainActivity::class.java))
                        finish()
                    } else {
                        // Se a tentativa de criação de conta falhar, exibe uma mensagem de erro
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

        }
    }

}
