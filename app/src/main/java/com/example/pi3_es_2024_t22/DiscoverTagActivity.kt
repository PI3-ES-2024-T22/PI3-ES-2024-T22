package com.example.pi3_es_2024_t22

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import android.view.View
import android.widget.ImageView
import java.io.ByteArrayOutputStream


class DiscoverTagActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var tagDataTextView: TextView
    private lateinit var writeButton: Button
    private lateinit var clientImage: ImageView
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover_tag)

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

//                tagDataTextView.visibility = View.GONE
                writeButton.visibility = View.VISIBLE
                clientImage.visibility = View.VISIBLE
                tagDataTextView.text = "O usuario a alocar o armario:"

                writeButton.setOnClickListener {
                    try {
                        val tagData = readImageFromTag(tag)
                        clientImage.setImageBitmap(tagData)
                    } catch (e: Exception) {
                        Toast.makeText(this, "Não foi possível ler os dados da tag.", Toast.LENGTH_SHORT).show()
                    }
                    bitmap = BitmapFactory.decodeResource(resources, R.drawable.imagem)
                    writeImageToTag(tag, bitmap)
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

//    private fun readTagData(tag: Tag): String {
//        val ndef = Ndef.get(tag)
//        ndef?.connect()
//        val ndefMessage = ndef?.ndefMessage
//        ndef?.close()
//
//        val payloadBytes = ndefMessage?.records?.firstOrNull()?.payload
//        val payloadText = payloadBytes?.decodeToString() ?: "Nenhum dado encontrado"
//
//        return payloadText
//    }



//    private fun writeTagData(tag: Tag) {
//        val ndef = Ndef.get(tag)
//        ndef?.connect()
//
//        // Cria um novo NdefMessage com o novo status
//        val newStatus = "FILIPE VAGAL CAIO VAGAL JAIME JIMMY VAGAL MEL VAGAL THIAGO MAIOR BAIANO" // Aqui você pode definir o novo status desejado
//        val newNdefMessage = NdefMessage(NdefRecord.createTextRecord(null, newStatus))
//
//        // Escreve a nova mensagem na tag NFC
//        ndef?.writeNdefMessage(newNdefMessage)
//
//        ndef?.close()
//    }

    private fun readImageFromTag(tag: Tag): Bitmap? {
        val ndef = Ndef.get(tag)
        ndef?.connect()
        val ndefMessage = ndef?.ndefMessage
        val payloadBytes = ndefMessage?.records?.firstOrNull()?.payload
        ndef?.close()

        // Convertendo os bytes recebidos de volta para um bitmap
        return payloadBytes?.let {
            BitmapFactory.decodeByteArray(it, 0, it.size)
        }
    }

    private fun writeImageToTag(tag: Tag, bitmap: Bitmap) {
        val ndef = Ndef.get(tag)
        ndef?.connect()

        // Convertendo a imagem em bytes para armazenamento na tag
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val imageBytes = stream.toByteArray()
        stream.close()

        // Escrevendo os bytes da imagem na tag NFC
        val imageRecord = NdefRecord.createMime("image/png", imageBytes)
        val newNdefMessage = NdefMessage(imageRecord)
        ndef?.writeNdefMessage(newNdefMessage)

        ndef?.close()
    }

//    -----------------------------------------------------------------------------------

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
//    }


}