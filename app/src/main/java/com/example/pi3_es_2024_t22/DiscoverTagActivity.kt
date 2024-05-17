package com.example.pi3_es_2024_t22

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DiscoverTagActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var tagDataTextView: TextView
    private lateinit var writeButton: Button
    private lateinit var clientImage: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover_tag)

        auth = FirebaseAuth.getInstance()
        tagDataTextView = findViewById(R.id.tagDataTextView)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        writeButton = findViewById(R.id.writeButton)
        clientImage = findViewById(R.id.tagData)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent?.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            tag?.let {
                Toast.makeText(this, "Tag detectada com sucesso", Toast.LENGTH_SHORT).show()

                writeButton.visibility = View.VISIBLE
                clientImage.visibility = View.VISIBLE
                tagDataTextView.text = "O usuário a alocar o armário:"


                // Mostrar na view as informações da locação pelo usuario ao escanear o QR code
                //Firebse collection Locacao_feita 

                //  Nao acho necessário
                // try {
                //     val res = readTagData(tag)
                //     tagDataTextView.text = res
                // } catch (e: Exception) {
                //     Toast.makeText(this, "Não foi possível ler os dados da tag.", Toast.LENGTH_SHORT).show()
                // }

                writeButton.setOnClickListener {
                    try {
                        writeTagData(tag)
                    } catch (e: Exception) {
                        Toast.makeText(this, "Não foi possível escrever os dados na tag.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE
        )
        val intentFilters = arrayOf(IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED))
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFilters, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }


    //Acho q nao preisa ler, apenas escrever
    // private fun readTagData(tag: Tag): String {
    //     val ndef = Ndef.get(tag)
    //     ndef?.connect()
    //     val ndefMessage = ndef?.ndefMessage
    //     ndef?.close()

    //     val payloadBytes = ndefMessage?.records?.firstOrNull()?.payload
    //     val payloadText = payloadBytes?.decodeToString() ?: "Nenhum dado encontrado"

    //     return payloadText
    // }

    private fun writeTagData(tag: Tag) {
        val currentUser = auth.currentUser
        currentUser?.let {
            // Escrever na tag a locação feita pelo usuario
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val userName = document.getString("Nome Completo")
                        val userData = "Usuário: $userName"
                        val newNdefMessage = NdefMessage(NdefRecord.createTextRecord(null, userData))

                        val ndef = Ndef.get(tag)
                        ndef?.connect()
                        ndef?.writeNdefMessage(newNdefMessage)
                        ndef?.close()

                        Toast.makeText(this, "Informações do usuário ($userName) gravadas na tag NFC com sucesso.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Documento não encontrado.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao acessar o Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

}