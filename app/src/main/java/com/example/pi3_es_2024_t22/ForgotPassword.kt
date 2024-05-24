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

class ForgotPassword : AppCompatActivity() {
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var buttonResetPassword: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewLogin: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Inicialização das views e do FirebaseAuth
        editTextEmail = findViewById(R.id.email)
        buttonResetPassword = findViewById(R.id.btn_reset_password)
        progressBar = findViewById(R.id.progressBar)
        textViewLogin = findViewById(R.id.loginNow)
        auth = FirebaseAuth.getInstance()

        // Define o OnClickListener para o texto de login
        textViewLogin.setOnClickListener {
            val intent = Intent(this@ForgotPassword, Login::class.java)
            startActivity(intent)
            finish()
        }

        // Define o OnClickListener para o botão de redefinir senha
        buttonResetPassword.setOnClickListener {
            val email: String = editTextEmail.text.toString()

            // Verifica se o campo de e-mail está vazio
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this@ForgotPassword, "Digite o seu email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Torna a barra de progresso visível
            progressBar.visibility = View.VISIBLE

            // Envio do e-mail de redefinição de senha usando o método sendPasswordResetEmail do FirebaseAuth
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        // Mensagem de sucesso ao enviar o e-mail de redefinição de senha
                        Toast.makeText(this@ForgotPassword, "Um email de recuperação de senha foi enviado para $email", Toast.LENGTH_SHORT).show()

                        // Redireciona para tela de login
                        val intent = Intent(applicationContext, Login::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        // Mensagem de falha ao enviar o e-mail de redefinição de senha
                        Toast.makeText(this@ForgotPassword, "Falha ao enviar email de recuperação de senha. Verifique o email e tente novamente.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    // Sobrescrita da função onBackPressed para retornar à tela de login ao pressionar o botão de voltar
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(applicationContext, Login::class.java)
        startActivity(intent)
        finish()
    }
}
