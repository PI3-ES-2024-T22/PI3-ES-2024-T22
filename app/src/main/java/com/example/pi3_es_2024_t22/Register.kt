package com.example.pi3_es_2024_t22

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
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
import com.google.firebase.firestore.FirebaseFirestore

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
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        editTextEmail = findViewById<TextInputEditText>(R.id.email).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        editTextPassword = findViewById<TextInputEditText>(R.id.password).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        editTextNome = findViewById(R.id.Nome_Completo)
        editTextNumero = findViewById<TextInputEditText>(R.id.N_Celular).apply {
            inputType = InputType.TYPE_CLASS_PHONE
        }
        editTextCPF = findViewById<TextInputEditText>(R.id.CPF).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        editTextDataNasc = findViewById<TextInputEditText>(R.id.Data_Nascimento).apply {
            inputType = InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_DATE
            filters = arrayOf(InputFilter.LengthFilter(8)) // Limita o campo a 8 caracteres
            onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    // Mostrar hint ao tocar no campo de texto
                    (view as TextInputEditText).hint = getString(R.string.data_nascimento_hint)
                } else {
                    // Ocultar hint ao perder o foco
                    (view as TextInputEditText).hint = null
                }
            }
        }
        buttonRegister = findViewById(R.id.btn_register)
        progressBar = findViewById(R.id.progressBar)
        textView = findViewById(R.id.loginNow)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

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
            val dataNascimento: String = editTextDataNasc.text.toString()
            val perfil: String = editTextPerfil.text.toString().toLowerCase() // Convertendo para minúsculo
            val cartao = "" // Defina o valor do cartão aqui

            if (TextUtils.isEmpty(nome) || !nome.matches("[a-zA-Z ]+".toRegex())) {
                showError("Digite o nome completo")
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(numero) || !numero.matches("[0-9]+".toRegex())) {
                showError("Digite o número de telefone completo")
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(cpf) || !cpf.matches("[0-9]+".toRegex())) {
                showError("Digite o número do seu CPF")
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(dataNascimento) || !dataNascimento.matches("[0-9]{2}/[0-9]{2}/[0-9]{2}".toRegex())) {
                showError("Digite a sua data de nascimento no formato 00/00/00")
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(perfil) || (perfil != "gerente" && perfil != "cliente")) { // Verificação em minúsculas
                showError("Digite uma das opções Gerente ou Cliente")
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(email)) {
                showError("Digite o email")
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                showError("Digite a senha")
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.let {
                            val userId = it.uid

                            val userMap = hashMapOf(
                                "Email" to email,
                                "Nome Completo" to nome,
                                "CPF" to cpf,
                                "Data de Nascimento" to dataNascimento,
                                "Perfil" to perfil,
                                "Cartao" to cartao
                            )

                            firestore.collection("Pessoas").document(userId)
                                .set(userMap)
                                .addOnSuccessListener {
                                    showToast("Autenticação realizada com sucesso.")
                                    startActivity(Intent(this@Register, MainActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    showToast("Falha ao salvar informações: ${e.message}")
                                    Log.e("Firestore", "Falha de permissão ao salvar informações: ${e.message}")
                                }
                        }
                    } else {
                        showToast("Falha na autenticação.")
                    }
                }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this@Register, message, Toast.LENGTH_SHORT).show()
        progressBar.visibility = View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(applicationContext, Login::class.java)
        startActivity(intent)
        finish()
    }
}
