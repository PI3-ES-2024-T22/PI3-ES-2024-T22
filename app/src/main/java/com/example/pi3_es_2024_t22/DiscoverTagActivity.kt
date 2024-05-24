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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DiscoverTagActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var tag: Tag? = null
    private var scannedData: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover_tag)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        scannedData = intent.getStringExtra("scannedData")

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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)

        if (intent?.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)

            tag?.let {
                if (scannedData != null) {
                    writeTagData(tag, scannedData!!)
                } else {
                    Toast.makeText(this, "Nenhuma tag NFC detectada ou dados a gravar estão ausentes", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun writeTagData(tag: Tag?, data: String) {
        if (tag == null) return

        val ndefMessage = NdefMessage(NdefRecord.createTextRecord(null, data))

        val ndef = Ndef.get(tag)
        ndef?.connect()
        ndef?.writeNdefMessage(ndefMessage)
        ndef?.close()

        Toast.makeText(this, "Dados gravados na tag NFC com sucesso", Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(applicationContext, ManagerMainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
