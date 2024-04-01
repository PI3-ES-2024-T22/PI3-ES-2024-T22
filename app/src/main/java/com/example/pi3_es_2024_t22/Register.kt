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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class Register : AppCompatActivity() {
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var editTextNome: TextInputEditText
    private lateinit var editTextNumero: TextInputEditText
    private lateinit var editTextCPF: TextInputEditText
    private lateinit var editTextDataNasc: TextInputEditText
    private lateinit var editTextPerfil: TextInputEditText
    private lateinit var buttonRegister: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textView: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        editTextNome = findViewById(R.id.Nome_Completo)
        editTextNumero = findViewById(R.id.N_Celular)
        editTextCPF = findViewById(R.id.CPF)
        editTextDataNasc = findViewById(R.id.Data_Nascimento)
        editTextPerfil = findViewById(R.id.Perfil_Usuario)
        buttonRegister = findViewById(R.id.btn_register)
        progressBar = findViewById(R.id.progressBar)
        textView = findViewById(R.id.loginNow)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Pessoas") //Acessa tabela Pessoas

        textView.setOnClickListener {
            val intent = Intent(this@Register, Login::class.java)
            startActivity(intent)
            finish()
        }

        buttonRegister.setOnClickListener {
            progressBar.visibility = View.VISIBLE

            val email: String = editTextEmail.text.toString()
            val password: String = editTextPassword.text.toString()
            val nome: String = editTextNome.text.toString()
            val numero: String = editTextNumero.text.toString()
            val cpf: String = editTextCPF.text.toString()
            val data_nascimento: String = editTextDataNasc.text.toString()
            val perfil: String = editTextPerfil.text.toString()

            if (TextUtils.isEmpty(nome) || !nome.matches("[a-zA-Z ]+".toRegex())) {
                Toast.makeText(this@Register, "Digite o nome completo", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(numero) || !numero.matches("[0-9]+".toRegex())) {
                Toast.makeText(this@Register, "Digite o número de telefone completo", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(cpf) || !cpf.matches("[0-9]+".toRegex())) {
                Toast.makeText(this@Register, "Digite o número do seu CPF", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(data_nascimento) || !data_nascimento.matches("[0-9]{2}/[0-9]{2}/[0-9]{2}".toRegex())) {
                Toast.makeText(this@Register, "Digite a sua data de nascimento no formato 00/00/00", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(perfil) || (perfil != "Gerente" && perfil != "Cliente")) {
                Toast.makeText(this@Register, "Digite uma das opções Gerente ou Cliente", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this@Register, "Digite o email", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this@Register, "Digite a senha", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.let {
                            val userId = it.uid
                            val userRef = database.child(userId) //Cria um ID do usuario dentro da tabela pessoas
                            userRef.child("Email").setValue(email) //Insere informacoes dentro do ID dentro da tabela PESSOAS
                            userRef.child("Nome Completo").setValue(nome)
                            userRef.child("CPF").setValue(cpf)
                            userRef.child("Data de Nascimento").setValue(data_nascimento)
                            userRef.child("Perfil").setValue(perfil)

                            Toast.makeText(
                                baseContext,
                                "Autenticação realizada com sucesso.",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(Intent(this@Register, MainActivity::class.java))
                            finish()
                        }
                    } else {
                        Toast.makeText(
                            baseContext,
                            "Falha na autenticação.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}
