package com.example.pi3_es_2024_t22

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pi3_es_2024_t22.databinding.ActivitySignUpBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.buttonSignUp.setOnClickListener() {

            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if (checkAllFields()) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Usuário criado com sucesso", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("SignUpActivity", "Error creating user", it.exception)
                    }
                }
            }
        }
    }

    private fun checkAllFields(): Boolean {

        val email = binding.etEmail.text.toString()

        if (binding.etEmail.text.toString().isEmpty()) {
            binding.textInputLayoutEmail.error = "Digite um email"
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textInputLayoutEmail.error = "Digite um email válido"
            return false
        }

        if (binding.etPassword.text.toString().isEmpty()) {
            binding.textInputLayoutPassword.error = "Digite uma senha"
            return false
        }

        if (binding.etPassword.text.toString().length < 6) {
            binding.textInputLayoutPassword.error = "A senha deve ter no mínimo 6 caracteres"
            return false
        }

        if (binding.etConfirmPassword.text.toString().isEmpty()) {
            binding.textInputLayoutConfirmPassword.error = "Confirme a senha"
            return false
        }

        if (binding.etPassword.text.toString() != binding.etConfirmPassword.text.toString()) {
            binding.textInputLayoutConfirmPassword.error = "As senhas não coincidem"
            return false
        }

        return true
    }
}