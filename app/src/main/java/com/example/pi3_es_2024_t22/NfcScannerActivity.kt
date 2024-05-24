package com.example.pi3_es_2024_t22

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.sql.Timestamp
import java.util.Calendar
import java.util.Date

class NfcScannerActivity : AppCompatActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var textView: TextView
    private var scannedData: String? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var quickOpenButton: Button
    private lateinit var finishLocation: Button
    private var tag: Tag? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("NfcScannerActivity", "onCreate called")
        setContentView(R.layout.activity_nfc_scanner)

        textView = findViewById(R.id.textView)
        firestore = FirebaseFirestore.getInstance()
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        quickOpenButton = findViewById(R.id.btnQuickOpenLocker)
        finishLocation = findViewById(R.id.btnfinishLocation)

        val tempoInicioLocacao = Date()

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -1)
        val tempoFimLocacao = calendar.time

        val caucao = calcularCaucao(tempoInicioLocacao, tempoFimLocacao)
        val userMap = hashMapOf(
            "EstornoCaucao" to caucao
        )

        quickOpenButton.setOnClickListener {
            Toast.makeText(this, "Armário aberto momentaneamente", Toast.LENGTH_SHORT).show()
        }

        finishLocation.setOnClickListener{
            // Erase the data from the scanned NFC tag
            eraseTagData(tag)
            calcularCaucao(tempoInicioLocacao, tempoFimLocacao)
        }

        if (nfcAdapter == null) {
            textView.text = "NFC is not available on this device."
            return
        }
    }

    private fun eraseTagData(tag: Tag?) {
        if (tag == null) return

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

        // 3. Consultar a tabela de preços para obter o preço correspondente
        val precoPorHora = when {
            tempoDecorridoHoras >= 4 -> 150.0
            tempoDecorridoHoras >= 2 -> 100.0
            tempoDecorridoHoras >= 1 -> 50.0
            else -> 0.0
        }

        // 4. Calcular o valor do caução
        return tempoDecorridoHoras * precoPorHora
    }




    override fun onResume() {
        super.onResume()
        Log.d("NfcScannerActivity", "onResume called")
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        val intentFilters = arrayOf<IntentFilter>(
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        )
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null)
    }

    override fun onPause() {
        super.onPause()
        Log.d("NfcScannerActivity", "onPause called")
        nfcAdapter.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)

        Log.d("NfcScannerActivity", "onNewIntent called")

        if (intent != null) {
            when (intent.action) {
                NfcAdapter.ACTION_NDEF_DISCOVERED,
                NfcAdapter.ACTION_TAG_DISCOVERED,
                NfcAdapter.ACTION_TECH_DISCOVERED -> {
                    Log.d("NfcScannerActivity", "NDEF discovered")
                    val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                    // tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                    Log.d("NfcScannerActivity", "Raw NFC tag data: $rawMsgs")

                    if (rawMsgs != null) {
                        val messages = rawMsgs.map { it as NdefMessage }.toTypedArray()
                        processNfcMessages(messages)
                    }
                }
                else -> Log.d("NfcScannerActivity", "Unexpected intent action: ${intent.action}")
            }

        } else {
            Log.d("NfcScannerActivity", "Received null intent in onNewIntent")
        }

        Log.d("NfcScannerActivity", "onNewIntent finished")
    }

    private fun processNfcMessages(messages: Array<NdefMessage>) {
        Log.d("NfcScannerActivity", "processNfcMessages called with ${messages.size} messages")
        if (messages.isEmpty()) return

        val record = messages[0].records[0]
        try {
            scannedData = readText(record)
            Log.d("NfcScannerActivity", "Scanned data: $scannedData")
        } catch (e: UnsupportedEncodingException) {
            Log.e("NfcScannerActivity", "Unsupported Encoding", e)
        }

        processScannedData(scannedData)
    }

    @Throws(UnsupportedEncodingException::class)
    private fun readText(record: NdefRecord): String {
        val payload = record.payload
        val textEncoding = if ((payload[0].toInt() and 128) == 0) "UTF-8" else "UTF-16"
        val languageCodeLength = payload[0].toInt() and 63
        return String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, Charset.forName(textEncoding))
    }

    private fun processScannedData(scannedData: String?) {
        // Check if scannedData is not null and fetch data from Firestore
        if (scannedData != null) {
            firestore.collection("locacoes").document(scannedData)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // Document exists, extract data
                        val localId = documentSnapshot.getString("localId")
                        val preco = documentSnapshot.getString("preco")
                        val isActive = documentSnapshot.getBoolean("ativo")
                        val photoUrl1 = documentSnapshot.getString("photoUrl1")
                        val photoUrl2 = documentSnapshot.getString("photoUrl2")

                        // Display the fetched data on the screen
                        val infoText = "Local ID: $localId\nPreço: $preco\nAtivo: $isActive"
                        textView.text = infoText

                        // Load and display images if URLs exist
                        if (!photoUrl1.isNullOrEmpty()) {
                            // Load and display image from photoUrl1
                            loadAndDisplayImage(photoUrl1, R.id.imageView1)
                        }

                        if (!photoUrl2.isNullOrEmpty()) {
                            // Load and display image from photoUrl2
                            loadAndDisplayImage(photoUrl2, R.id.imageView2)
                        }
                        quickOpenButton.visibility = Button.VISIBLE
                    } else {
                        textView.text = "No data found for ID: $scannedData"
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("NfcScannerActivity", "Error fetching document", exception)
                    textView.text = "Error: ${exception.message}"
                }
        }
    }

    private fun loadAndDisplayImage(photoUrl: String, imageViewId: Int) {
        // Load and display image using Glide library
        val imageView = findViewById<ImageView>(imageViewId)
        Glide.with(this)
            .load(photoUrl)
            .into(imageView)
    }
}

