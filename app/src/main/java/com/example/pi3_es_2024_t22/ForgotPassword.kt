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

    private lateinit var database: DatabaseReference
    private lateinit var editEmail: editEmail
    private lateinit var btn_reset: btn_reset
    private lateinit var btn_back: editEmail
    private lateinit var progressBar: ProgressBar
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.ForgotPassword)

        database = FirebaseDatabase.getInstance().getReference("Pessoas")


        btn_reset.setOnClickListener {

            val user = FirebaseAuth.getInstance().currentUser
            user?.let {
                val userID = it.uid
                val userRef = database.child(userID) 
                //userRef.child("Nome").setValue(cardForm.cardholderName)
            }
        }
    }
}
