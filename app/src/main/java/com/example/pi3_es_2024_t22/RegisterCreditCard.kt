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
import android.graphics.Color


class RegisterCreditCard : AppCompatActivity() {
    // Declaração das variáveis
    private lateinit var cardForm: CardForm
    private lateinit var firestore: FirebaseFirestore
    private lateinit var btn_concluir: Button
    private var userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Ativa a funcionalidade de tela cheia com gestos de borda a borda
        setContentView(R.layout.activity_register_credit_card)

        // Inicialização das variáveis
        cardForm = findViewById(R.id.cardForm)
        btn_concluir = findViewById(R.id.btn_concluir)
        firestore = FirebaseFirestore.getInstance()

        // Configuração do formulário de cartão de crédito
        cardForm.cardRequired(true)
            .expirationRequired(true)
            .cvvRequired(true)
            .cardholderName(CardForm.FIELD_REQUIRED)
            .actionLabel("Concluir")
            .setup(this@RegisterCreditCard)

        // Definir a cor do texto dos EditTexts como branca
        val cardholderNameEditText = cardForm.cardEditText
        val cardNumberEditText = cardForm.cardholderNameEditText
        val expirationEditText = cardForm.expirationDateEditText
        val cvvEditText = cardForm.cvvEditText

        cardholderNameEditText.setTextColor(Color.parseColor("#FFFFFF"))
        cardNumberEditText.setTextColor(Color.parseColor("#FFFFFF"))
        expirationEditText.setTextColor(Color.parseColor("#FFFFFF"))
        cvvEditText.setTextColor(Color.parseColor("#FFFFFF"))


        // Configuração do campo de CVV
        cardForm.cvvEditText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD

        // Configuração do evento de clique para o botão de conclusão
        btn_concluir.setOnClickListener {
            if (cardForm.isValid) { // Verifica se o formulário de cartão é válido
                Toast.makeText(this@RegisterCreditCard, "Cartão válido", Toast.LENGTH_LONG).show()

                val user = FirebaseAuth.getInstance().currentUser
                user?.let {
                    val userID = it.uid

                    // Define uma classe para representar os detalhes do cartão
                    data class Cartao(
                        val nomeCartao: String,
                        val numeroCartao: String,
                    )

                    // Cria uma instância da classe Cartao com os detalhes do cartão fornecidos pelo formulário
                    val cartao = Cartao(
                        nomeCartao = cardForm.cardholderName,
                        numeroCartao = cardForm.cardNumber
                    )

                    // Atualiza as informações do cartão no Firestore
                    firestore.collection("Pessoas").document(userID)
                        .update("Cartao", cartao)
                        .addOnSuccessListener {
                            Toast.makeText(this@RegisterCreditCard, "Informações do cartão salvas com sucesso", Toast.LENGTH_LONG).show()

                            // Redireciona para a próxima atividade após salvar as informações do cartão
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

    // Lidar com o comportamento do botão de voltar
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
