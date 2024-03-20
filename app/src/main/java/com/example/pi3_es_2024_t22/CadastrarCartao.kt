package com.example.pi3_es_2024_t22

import android.os.Bundle
import android.widget.Toast
import android.view.inputmethod.InputType
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.braintreepayments.cardform.view.CardForm
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_cadastrar_cartao.*

class CadastrarCartao : AppCompatActivity() {

    private lateinit var cardForm: CardForm
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cadastrar_cartao)

        cardForm = findViewById(R.id.cardForm)
        database = FirebaseDatabase.getInstance().getReference("Pessoas")

        cardForm.cardRequired(true)
            .expirationRequired(true)
            .cvvRequired(true)
            .cardholderName(CardForm.FIELD_REQUIRED)
            .actionLabel("Purchase")
            .celphoneRequired(true)
            .cpfRequired(true)
            .setup(this@CadastrarCartao)

        cardForm.cvvEditText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD

        btn_concluir.setOnClickListener {
            if (cardForm.isValid) {
                Toast.makeText(this@CadastrarCartao, "Cartao valido", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@CadastrarCartao, "Cartao invalido, por favor preencha novamente os campos", Toast.LENGTH_LONG).show()
            }
            
            val user = FirebaseAuth.getInstance().currentUser
            user?.let {
                val userID = it.uid
                val userRef = database.child(userID)
                userRef.child("Nome").setValue(cardForm.cardholderName)
                userRef.child("Numero").setValue(cardForm.cardNumber)
                userRef.child("CPF").setValue(cardForm.cpf)
                userRef.child("Celular").setValue(cardForm.celphone)
            }
        }
    }
}
