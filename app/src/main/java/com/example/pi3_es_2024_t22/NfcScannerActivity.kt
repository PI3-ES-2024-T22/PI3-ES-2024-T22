package com.example.pi3_es_2024_t22

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class NfcScannerActivity : AppCompatActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var textView: TextView
    private var scannedData: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("NfcScannerActivity", "onCreate called")
        setContentView(R.layout.activity_nfc_scanner)

        textView = findViewById(R.id.textView)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            textView.text = "NFC is not available on this device."
            return
        }
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

        val builder = StringBuilder()
        for (message in messages) {
            for (record in message.records) {
                try {
                    val payload = readText(record)
                    builder.append(payload).append("\n")
                } catch (e: UnsupportedEncodingException) {
                    Log.e("NfcScannerActivity", "Unsupported Encoding", e)
                }
            }
        }
        scannedData = builder.toString()
        Log.d("NfcScannerActivity", "Scanned data: $scannedData")
        textView.text = scannedData
    }

    @Throws(UnsupportedEncodingException::class)
    private fun readText(record: NdefRecord): String {
        val payload = record.payload
        val textEncoding = if ((payload[0].toInt() and 128) == 0) "UTF-8" else "UTF-16"
        val languageCodeLength = payload[0].toInt() and 63
        return String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, Charset.forName(textEncoding))
    }
}
