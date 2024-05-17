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
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class DiscoverTagActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var tagDataTextView: TextView
    private lateinit var writeButton: Button
    private lateinit var clientImage: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var clienteInfo: TextView
    private lateinit var prosseguirButton: Button
    private lateinit var encerrarButton: Button
    private lateinit var abrirArmarioButton: Button
    private lateinit var encerramentoTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover_tag)

        auth = FirebaseAuth.getInstance()
        tagDataTextView = findViewById(R.id.tagDataTextView)
        clienteInfo = findViewById(R.id.tagClienteInfo)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        writeButton = findViewById(R.id.writeButton)
        clientImage = findViewById(R.id.tagData)
        db = FirebaseFirestore.getInstance()
        prosseguirButton = findViewById(R.id.prosseguirButton)
        encerrarButton = findViewById(R.id.encerrarButton)
        abrirArmarioButton = findViewById(R.id.abrirArmarioButton)
        encerramentoTextView = findViewById(R.id.EncerrarTextoCliente)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent?.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            tag?.let {

                writeButton.visibility = View.VISIBLE
                clientImage.visibility = View.VISIBLE
                tagDataTextView.text = "O usuário a alocar o armário:"

                val currentUser = auth.currentUser
                currentUser?.let { user ->
                    db.collection("locacao_pessoa").document(user.uid).get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                val userName = document.getString("Nome Completo")
                                val local = document.getString("address") + ", " + document.getString("referencePoint")
                                val clientPhotoLink = document.getString("clientPhotoLink")
                                //val tempoInicioLocacao = document.getDate("tempoInicioLocacao")

                                val userData = "Usuário: $userName\nLocal: $local"
                                clienteInfo.text = userData

                                try {
                                    Glide.with(this).load(clientPhotoLink).into(clientImage)
                                } catch (e: Exception) {
                                    Log.e("DiscoverTagActivity", "Erro ao carregar a imagem", e)
                                }

                                writeButton.setOnClickListener {
                                    writeButton.visibility = View.GONE
                                    findViewById<Button>(R.id.prosseguirButton).visibility = View.VISIBLE

                                    try {
                                        writeTagData(tag, userName, local, clientPhotoLink)
                                    } catch (e: Exception) {
                                        Toast.makeText(this, "Mantenha a TAG próxima ao celular", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(this, "Usuário não realizou nenhuma locação.", Toast.LENGTH_SHORT).show()
                            }

                            prosseguirButton.setOnClickListener {
                                findViewById<Button>(R.id.encerrarButton).visibility = View.VISIBLE
                                findViewById<Button>(R.id.abrirArmarioButton).visibility = View.VISIBLE
                                prosseguirButton.visibility = View.GONE
                            }

                            abrirArmarioButton.setOnClickListener {

                            }

                            encerrarButton.setOnClickListener {

                                try {
                                    eraseTagData(tag)

                                } catch (e: Exception) {
                                    Toast.makeText(this, "Mantenha a TAG próxima ao celular", Toast.LENGTH_SHORT).show()
                                }

                                val tempoFimLocacao = Date()

                                val tempoInicioLocacaoTimestamp = document.getTimestamp("tempoInicioLocacao")
                                val tempoInicioLocacao = tempoInicioLocacaoTimestamp?.toDate()

                                val caucao = calcularCaucao(tempoInicioLocacao, tempoFimLocacao)
                                currentUser?.let { user ->
                                    val userMap = hashMapOf(
                                        "EstornoCaucao" to caucao
                                    )

                                    db.collection("locacao_pessoa").document(user.uid).set(userMap)
                                }
                            }
                        }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE)
        val intentFilters = arrayOf(IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED))
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFilters, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    private fun writeTagData(tag: Tag, userName: String?, local: String?, clientPhotoLink: String?) {
        val userData = "Usuário: $userName\nLocal: $local\nImagem: $clientPhotoLink"
        val newNdefMessage = NdefMessage(NdefRecord.createTextRecord(null, userData))

        val ndef = Ndef.get(tag)
        ndef?.connect()
        ndef?.writeNdefMessage(newNdefMessage)
        ndef?.close()

        Toast.makeText(this, "Informações do usuário ($userName) gravadas na tag NFC com sucesso.", Toast.LENGTH_SHORT).show()
    }

    private fun eraseTagData(tag: Tag?) {
        if (tag == null){
            return
        }

        val newNdefMessage = NdefMessage(NdefRecord.createTextRecord(null, " "))

        val ndef = Ndef.get(tag)
        ndef?.connect()
        ndef?.writeNdefMessage(newNdefMessage)
        ndef?.close()

        Toast.makeText(this, "Os dados da TAG foram apagados com sucesso", Toast.LENGTH_SHORT).show()
    }
    private fun calcularCaucao(tempoInicioLocacao: Date?, tempoFimLocacao: Date): Double {
        tempoInicioLocacao ?: return 0.0 // Retorna 0 se o tempo de início da locação não estiver disponível

        // 1. Calcular o tempo decorrido em milissegundos
        val tempoDecorridoMillis = tempoFimLocacao.time - tempoInicioLocacao.time

        // 2. Converter o tempo decorrido de milissegundos para horas
        val tempoDecorridoHoras = tempoDecorridoMillis / 3600000.0 // 3600000 ms = 1 hora

        // 3. Consultar a tabela de preços para obter o preço correspondente ao tempo decorrido

        val precoPorHora = when {
            tempoDecorridoHoras >= 4 -> 150.0
            tempoDecorridoHoras >= 2 -> 100.0
            tempoDecorridoHoras >= 1 -> 50.0
            else -> 0.0
        }

        // 4. Calcular o valor do caução
        return tempoDecorridoHoras * precoPorHora
    }
}

