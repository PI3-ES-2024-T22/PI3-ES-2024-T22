package com.example.pi3_es_2024_t22

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.nfc.tech.Ndef

class DiscoverTagActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_discover_tag)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this) // Referencia o adaptador NFC do dispositivo
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {
            processNdefMessages(intent)
        }
    }

    private fun processNdefMessages(intent: Intent) {
        intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMessages -> //Cria um array de parcelable com os dados da tag -> estrutura de dados p/ transitar entre componentes de um app android ( services, activity e broadcast). Intents carregam dados de forma "global" com os arrays de parcelables
            val messages: List<NdefMessage> = rawMessages.map { it as NdefMessage } //Transforma os dados da tag na lista de parcelable para dados do tipo NDEFMESSAGE -> P/ tratar esses dados

            // Processa o conteudo dentro da mensagem NDEF
            for (message in messages) { //
                for (record in message.records) { //Record eh a estrutura de dados de uma mensagem NEDF -> dentro pode ter Payload, Tipo, Tamanho
                    // Processar o conteúdo de cada registro, se necessário
                    //Verificar se tag corresponde ao armario tal do banco de dados
                }
            }

            displayProcessedMessages(messages)

            //Se sim -> Alterar o dado da TAG NFC para o proximo scan
            val newText = "Nova mensagem na tag"
            val newRecord = NdefRecord.createTextRecord(null, newText)
            val newMessage = NdefMessage(arrayOf(newRecord))

            // Escrever a nova mensagem na tag NFC, se desejar
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            tag?.let {

                writeNdefMessage(it, newMessage) // Aqui estamos trocando de fato os dados
            }
        }
    }

    private fun displayProcessedMessages(messages: List<NdefMessage>) {
        // Implementar a lógica para exibir as mensagens processadas, se necessário
    }

    private fun writeNdefMessage(tag: Tag, message: NdefMessage) {
        val ndef = Ndef.get(tag)
        ndef?.let {
            it.connect()
            it.writeNdefMessage(message)
            it.close()
        }
    }
}
