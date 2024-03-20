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
import com.google.firebase.database.DatabaseReference //Bibliotecas para usar Real Time DataBase do FireBase
import com.google.firebase.database.FirebaseDatabase //Bibliotecas para usar Real Time DataBase do FireBase

class Register : AppCompatActivity() {
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var buttonRegister: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textView: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference // Declarando a variavel DatabaseReference que vai linkar o banco com o back

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Associa as variáveis aos elementos do layout XML
        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        buttonRegister = findViewById(R.id.btn_register)
        progressBar = findViewById(R.id.progressBar)
        textView = findViewById(R.id.loginNow)

        auth = FirebaseAuth.getInstance() //Inicializa a instância do FirebaseAuth

        database = FirebaseDatabase.getInstance().getReference("Pessoas") // Inicializa a instância do Database Reference criando tabela Pessoas

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

                        //Logica para salvar no RealTime DataBase
                        val user = auth.currentUser //Obtem o usuario atualmente autenticado
                        user?.let {
                            val userId = it.uid //Pega ID do usuario atual autenticado
                            val userRef = database.child(userId) //Conecta banco de dados e a tabela Pessoas para o Usuario autenticado no momento
                            userRef.child("email").setValue(email) //Salva no banco de dados o email do usuario atual autenticado
                        
                            Toast.makeText(
                                baseContext,
                                "Authentication was successful.",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(Intent(this@Register, MainActivity::class.java))
                            finish()
                        }
                    } else {
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
