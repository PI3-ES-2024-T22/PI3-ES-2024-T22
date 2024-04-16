package com.example.pi3_es_2024_t22

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.text.InputType
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.braintreepayments.cardform.view.CardForm
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Button

class RegisterCreditCard : AppCompatActivity() {

    private lateinit var cardForm: CardForm
    private lateinit var firestore: FirebaseFirestore
    private lateinit var btn_concluir: Button
    private var userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_credit_card)

        cardForm = findViewById(R.id.cardForm)
        btn_concluir = findViewById(R.id.btn_concluir)
        firestore = FirebaseFirestore.getInstance()

        cardForm.cardRequired(true)
            .expirationRequired(true)
            .cvvRequired(true)
            .cardholderName(CardForm.FIELD_REQUIRED)
            .actionLabel("Concluir")
            .mobileNumberRequired(true)
            .setup(this@RegisterCreditCard)

        cardForm.cvvEditText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD

        btn_concluir.setOnClickListener {
            if (cardForm.isValid) {
                Toast.makeText(this@RegisterCreditCard, "Cartão válido", Toast.LENGTH_LONG).show()

                val user = FirebaseAuth.getInstance().currentUser
                user?.let {
                    val userID = it.uid

                    data class Cartao(
                        val nomeCartao: String,
                        val numeroCartao: String,
                    )

                    val cartao = Cartao(
                        nomeCartao = cardForm.cardholderName,
                        numeroCartao = cardForm.cardNumber
                    )

                    firestore.collection("Pessoas").document(userID)
                        .update("Cartao", cartao)
                        .addOnSuccessListener {
                            Toast.makeText(this@RegisterCreditCard, "Informações do cartão salvas com sucesso", Toast.LENGTH_LONG).show()

                            val intent = Intent(applicationContext, MapsActivity::class.java)
                            startActivity(intent)
                            finish()

                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this@RegisterCreditCard, "Erro ao salvar informações do cartão: $e", Toast.LENGTH_LONG).show()
                        }
                }
            } else {
                Toast.makeText(this@RegisterCreditCard, "Cartão inválido, por favor, preencha novamente os campos", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
