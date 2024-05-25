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
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

// Classe para lidar com a leitura e escrita de tags NFC
class NfcScannerActivity : AppCompatActivity() {

    // Declaração de variáveis
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var textView: TextView
    private var scannedData: String? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var quickOpenButton: Button
    private lateinit var finishLocation: Button
    private var scannedTag: Tag? = null

    // Método chamado quando a atividade é criada
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_scanner)

        // Inicialização de variáveis
        textView = findViewById(R.id.textView)
        firestore = FirebaseFirestore.getInstance()
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        quickOpenButton = findViewById(R.id.btnQuickOpenLocker)
        finishLocation = findViewById(R.id.btnfinishLocation)

        // Configuração dos botões
        quickOpenButton.setOnClickListener {
            Toast.makeText(this, "Armário aberto momentaneamente", Toast.LENGTH_SHORT).show()
        }

        finishLocation.setOnClickListener {
            try {
                if (scannedData != null) {
                    calcularCaucao(scannedData!!)
                } else {
                    Toast.makeText(this, "Nenhuma tag NFC foi escaneada", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Mantenha a TAG próxima ao celular", Toast.LENGTH_SHORT).show()
            }
        }

        // Verificar se o adaptador NFC está disponível
        if (nfcAdapter == null) {
            textView.text = "NFC is not available on this device."
            return
        }
    }

    // Método para apagar os dados da tag NFC escaneada
    private fun eraseTagData() {
        if (scannedTag != null) {
            try {
                val ndef = Ndef.get(scannedTag)
                ndef?.connect()
                val newNdefMessage = NdefMessage(NdefRecord.createTextRecord(null, ""))
                ndef?.writeNdefMessage(newNdefMessage)
                ndef?.close()
                Toast.makeText(
                    this,
                    "Os dados da TAG foram apagados com sucesso",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: IOException) {
                Toast.makeText(
                    this,
                    "Erro ao conectar com a tag NFC. Por favor, tente novamente.",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Mantenha a TAG próxima ao celular", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Nenhuma tag NFC foi escaneada", Toast.LENGTH_SHORT).show()
        }

        // Apagar o documento correspondente à tag NFC do Firestore
        firestore.collection("locacoes").document(scannedData!!)
            .delete()
            .addOnSuccessListener {
                Log.d("Firestore", "Documento apagado com sucesso")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Erro ao apagar documento", e)
            }
    }

    // Método chamado quando a atividade é retomada
    override fun onResume() {
        super.onResume()
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        val intentFilters = arrayOf<IntentFilter>(
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        )
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null)
    }

    // Método chamado quando a atividade é pausada
    override fun onPause() {
        super.onPause()
        nfcAdapter.disableForegroundDispatch(this)
    }

    // Método chamado quando uma nova intenção é recebida
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)

        if (intent != null) {
            when (intent.action) {
                NfcAdapter.ACTION_NDEF_DISCOVERED,
                NfcAdapter.ACTION_TAG_DISCOVERED,
                NfcAdapter.ACTION_TECH_DISCOVERED -> {
                    val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)

                    if (rawMsgs != null) {
                        val messages = rawMsgs.map { it as NdefMessage }.toTypedArray()
                        processNfcMessages(messages)
                    }
                }
            }
        }
    }

    // Método para processar as mensagens NFC recebidas
    private fun processNfcMessages(messages: Array<NdefMessage>) {
        if (messages.isEmpty()) return

        val record = messages[0].records[0]
        try {
            scannedData = readText(record)
        } catch (e: UnsupportedEncodingException) {
            Log.e("NfcScannerActivity", "Unsupported Encoding", e)
        }
        intent?.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)?.let { scannedTag = it }
        processScannedData(scannedData)
    }

    // Método para ler o texto de um registro NDEF
    @Throws(UnsupportedEncodingException::class)
    private fun readText(record: NdefRecord): String {
        val payload = record.payload
        val textEncoding = if ((payload[0].toInt() and 128) == 0) "UTF-8" else "UTF-16"
        val languageCodeLength = payload[0].toInt() and 63
        return String(
            payload,
            languageCodeLength + 1,
            payload.size - languageCodeLength - 1,
            Charset.forName(textEncoding)
        )
    }

    // Método para processar os dados escaneados
    private fun processScannedData(scannedData: String?) {
        if (scannedData != null) {
            firestore.collection("locacoes").document(scannedData)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val localId = documentSnapshot.getString("localId")
                        val photoUrl1 = documentSnapshot.getString("photoUrl1")
                        val photoUrl2 = documentSnapshot.getString("photoUrl2")
                        firestore.collection("locais").document(localId!!)
                            .get()
                            .addOnSuccessListener { documentSnapshot ->
                                if (documentSnapshot.exists()) {
                                    val info = documentSnapshot.get("info") as? Map<String, Any>
                                    if (info != null) {
                                        val referencia = info["referencePoint"] as? String
                                        val endereco = info["address"] as? String
                                        val infoText = "Local: $referencia\n Endereço: $endereco\n"

                                        textView.text = infoText
                                    }
                                }
                            }

                        // Load and display images if URLs exist
                        if (!photoUrl1.isNullOrEmpty()) {
                            loadAndDisplayImage(photoUrl1, R.id.imageView1)
                        }

                        if (!photoUrl2.isNullOrEmpty()) {
                            loadAndDisplayImage(photoUrl2, R.id.imageView2)
                        }
                        quickOpenButton.visibility = Button.VISIBLE
                        finishLocation.visibility = Button.VISIBLE
                    } else {
                        textView.text = "TAG não encontrada no Firestore"
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("NfcScannerActivity", "Error fetching document", exception)
                    textView.text = "Error: ${exception.message}"
                }
        }
    }

    // Método para carregar e exibir uma imagem a partir de uma URL
    private fun loadAndDisplayImage(photoUrl: String, imageViewId: Int) {
        val imageView = findViewById<ImageView>(imageViewId)
        Glide.with(this)
            .load(photoUrl)
            .into(imageView)
    }

    // Método para calcular o valor do estorno
    private fun calcularCaucao(scannedData: String) {
        var totalCaucao = 1000.0 // Valor de uma diária

        firestore.collection("locacoes").document(scannedData)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val preco = documentSnapshot.getString("preco")?.toDoubleOrNull()
                    if (preco != null) {
                        totalCaucao -= preco
                    }

                    // Criar um novo campo na coleção "estornos" e salvar o valor de totalCaucao
                    val estornoData = hashMapOf(
                        "totalCaucao" to totalCaucao,
                        "ID" to scannedData
                    )

                    firestore.collection("estornos")
                        .add(estornoData)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Dados de estorno salvos com sucesso")
                            eraseTagData() // Chama a função eraseTagData aqui após salvar o estorno
                            Toast.makeText(this, "Estorno realizado com sucesso", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Erro ao salvar dados de estorno", e)
                            Toast.makeText(this, "Erro ao realizar estorno", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Log.e("NfcScannerActivity", "Documento não encontrado no Firestore")
                }

            }
            .addOnFailureListener { exception ->
                Log.e("NfcScannerActivity", "Erro ao buscar documento: ", exception)
            }

    }
}