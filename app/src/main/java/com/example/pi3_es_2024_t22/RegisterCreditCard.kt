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
        //val userRef = FirebaseDatabase.getInstance().getReference("Pessoas")

        database = FirebaseAuth.getInstance().currentUser?.uid?.let { //Pegar o ID do usuário logado
            FirebaseDatabase.getInstance().getReference("Pessoas").child(it) //Pegar a referência DA TABELA PESSOAS do usuário logado
        } 

        btn_concluir = findViewById(R.id.btn_concluir)

        cardForm.cardRequired(true)
            .expirationRequired(true)
            .cvvRequired(true)
            .cardholderName(CardForm.FIELD_REQUIRED)
            .actionLabel("Concluir")
            .mobileNumberRequired(true)
            .setup(this@RegisterCreditCard)

            //Falta resolver isso -> Mudança do idioma das validações dos campos de inserções
            // cardForm.setCardNumberError("Número de cartão inválido")
            // cardForm.setExpirationError("Data de validade inválida")
            // cardForm.setCvvError("CVV inválido")
            // cardForm.setCardholderNameError("Nome do titular inválido")
            // cardForm.setMobileNumberError("Número de celular inválido")

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
                        val numeroCartao: String,
                    )

                    val cartao = Cartao(
                        nomeCartao = cardForm.cardholderName,
                        numeroCartao = cardForm.cardNumber
                    )

                    userRef.child("Cartão").setValue(cartao)

                    //Após cadastrar cartão -> continua com o app
                    // val intent = Intent(applicationContext, AlocarArmario::class.java)
                    // startActivity(intent)
                    // finish()
                }
            } else {
                Toast.makeText(this@RegisterCreditCard, "Cartão inválido, por favor, preencha novamente os campos", Toast.LENGTH_LONG).show()
            }
        }
    }
}

//Quero atingir esse resultado no BD
// {
//     "Pessoas": {
//       "userID_1": {
//         "Email": "usuario1@example.com",
//         "Nome Completo": "Fulano de Tal",
//         "CPF": "123.456.789-00",
//         "Data de Nascimento": "01/01/1990",
//         "Perfil": "Cliente",
//         "Cartao": {
//           "nomeCartao": "Fulano de Tal",
//           "numeroCartao": "1234 5678 9012 3456",
//         }
//       },
//       "userID_2": {
//         "Email": "usuario2@example.com",
//         "Nome Completo": "Ciclano de Tal",
//         "CPF": "987.654.321-00",
//         "Data de Nascimento": "02/02/1995",
//         "Perfil": "Gerente",
//         "Cartao": {
//           "nomeCartao": "Ciclano de Tal",
//           "numeroCartao": "9876 5432 1098 7654",
//           "dataValidade": "05/23",
//           "cvv": "456"
//         }
//       }
//     }
//   }
  