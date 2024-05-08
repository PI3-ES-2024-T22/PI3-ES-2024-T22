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
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DiscoverTagActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var tagDataTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover_tag)

        tagDataTextView = findViewById(R.id.tagDataTextView)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent?.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            tag?.let {
                Toast.makeText(this, "Tag detectada com sucesso", Toast.LENGTH_SHORT).show()

                try {
                    val tagData = readTagData(tag)
                    tagDataTextView.text = tagData
                } catch (e: Exception) {
                    Toast.makeText(this, "Não foi possível ler os dados da tag.", Toast.LENGTH_SHORT).show()
                }

                // Aqui você chama a função para escrever na tag
                writeTagData(tag)
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

    private fun readTagData(tag: Tag): String {
        val ndef = Ndef.get(tag)
        ndef?.connect()
        val ndefMessage = ndef?.ndefMessage
        ndef?.close()

        val payloadBytes = ndefMessage?.records?.firstOrNull()?.payload
        val payloadText = payloadBytes?.decodeToString() ?: "Nenhum dado encontrado"

        return payloadText
    }

    private fun writeTagData(tag: Tag) {
        val ndef = Ndef.get(tag)
        ndef?.connect()

        // Cria um novo NdefMessage com o novo status
        val newStatus = "FILIPE VAGAL CAIO VAGAL JAIME JIMMY VAGAL MEL VAGAL THIAGO MAIOR BAIANO" // Aqui você pode definir o novo status desejado
        val newNdefMessage = NdefMessage(NdefRecord.createTextRecord(null, newStatus))

        // Escreve a nova mensagem na tag NFC
        ndef?.writeNdefMessage(newNdefMessage)

        ndef?.close()
    }


//PARA MAIS DETALHES DOS DADOS DA TAG
//    private fun readTagData(tag: Tag): String? {
//        val ndef = Ndef.get(tag)
//        ndef?.connect()
//        val ndefMessage = ndef?.ndefMessage
//        val stringBuilder = StringBuilder()
//        ndefMessage?.records?.forEachIndexed { index, record ->
//            stringBuilder.append("Record $index:\n")
//            stringBuilder.append("TNF: ${record.tnf}\n")
//            stringBuilder.append("Type: ${String(record.type)}\n")
//            stringBuilder.append("Payload: ${String(record.payload)}\n")
//        }
//        ndef?.close()
//        return stringBuilder.toString()
//    }


}
