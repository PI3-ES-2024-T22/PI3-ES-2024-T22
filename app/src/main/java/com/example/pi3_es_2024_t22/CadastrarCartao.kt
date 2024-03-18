package com.example.pi3_es_2024_t22

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.braintreepayments.cardform.view.CardForm //Biblioteca do braintreepayment para usar os forms
import com.google.firebase.auth.FirebaseAuth

class CadastrarCartao : AppCompatActivity() {

    private lateinit var cardForm: CardForm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cadastrar_cartao)

        //TALVEZ SALVAR NO BD O NOME DO CARTAO COMO NOME DO PESSOAS

        cardForm = findViewById(R.id.cardForm)

        //Funcoes de validacao para as caixas de texto
        cardForm.cardRequired(true)
            .expirationRequired(true)
            .cvvRequired(true)
            .cardholderName(CardForm.FIELD_REQUIRED) //Nao espera true or false
            .actionLabel("Purchase")//Talvez tirar
            .setup(this@CadastrarCartao //Setup do formulario dentro dessa activity

                    cardForm.cvvEditText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
    }
}
