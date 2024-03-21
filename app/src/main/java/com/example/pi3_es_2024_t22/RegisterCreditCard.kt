package com.example.pi3_es_2024_t22

import android.os.Bundle
import android.widget.Toast
import android.text.InputType
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.braintreepayments.cardform.view.CardForm
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.widget.Button
import androidx.core.view.isEmpty

class RegisterCreditCard : AppCompatActivity() {

    private lateinit var cardForm: CardForm
    private lateinit var database: DatabaseReference
    private lateinit var btn_concluir: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_credit_card)

        cardForm = findViewById(R.id.cardForm)
        database = FirebaseDatabase.getInstance().getReference("Pessoas")
        btn_concluir = findViewById(R.id.btn_concluir)

        cardForm.cardRequired(true)
            .expirationRequired(true)
            .cvvRequired(true)
            .cardholderName(CardForm.FIELD_REQUIRED)
            .actionLabel("Concluir")
            .mobileNumberRequired(true)
            .setup(this@RegisterCreditCard)

            //Falta resolver isso
            cardForm.setCardNumberError("Número de cartão inválido")
            cardForm.setExpirationError("Data de validade inválida")
            cardForm.setCvvError("CVV inválido")
            cardForm.setCardholderNameError("Nome do titular inválido")
            cardForm.setMobileNumberError("Número de celular inválido")

        cardForm.cvvEditText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD

        btn_concluir.setOnClickListener {
            if (cardForm.isValid) {
                Toast.makeText(this@RegisterCreditCard, "Cartão válido", Toast.LENGTH_LONG).show()

                val user = FirebaseAuth.getInstance().currentUser
                user?.let {
                    val userID = it.uid
                    val userRef = database.child(userID)

                    data class Cartao(
                        val nomeCartao: String,
                        val numeroCartao: String
                    )

                    val cartao = Cartao(
                        nomeCartao = cardForm.cardholderName,
                        numeroCartao = cardForm.cardNumber
                    )

                    userRef.child("Cartão ").setValue(cartao)
                }
            } else {
                Toast.makeText(this@RegisterCreditCard, "Cartão inválido, por favor, preencha novamente os campos", Toast.LENGTH_LONG).show()
            }
        }
    }
}
