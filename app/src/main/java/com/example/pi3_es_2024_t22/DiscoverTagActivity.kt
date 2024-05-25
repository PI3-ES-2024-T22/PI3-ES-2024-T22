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

// Classe para descobrir e interagir com tags NFC
class DiscoverTagActivity : AppCompatActivity() {

    // Declaração de variáveis
    private var nfcAdapter: NfcAdapter? = null
    private var tag: Tag? = null
    private var scannedData: String? = null

    // Método chamado quando a atividade é criada
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover_tag)

        // Inicialização do adaptador NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        // Recuperação dos dados escaneados da intenção
        scannedData = intent.getStringExtra("scannedData")
    }

    // Método chamado quando a atividade é retomada
    override fun onResume() {
        super.onResume()
        // Configuração do despacho em primeiro plano para o adaptador NFC
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE
        )
        val intentFilters = arrayOf(IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED))
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFilters, null)
    }

    // Método chamado quando a atividade é pausada
    override fun onPause() {
        super.onPause()
        // Desabilitar o despacho em primeiro plano para o adaptador NFC
        nfcAdapter?.disableForegroundDispatch(this)
    }

    // Método chamado quando uma nova intenção é recebida
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)

        // Verificar se a ação da intenção é a descoberta de uma tag NFC
        if (intent?.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            // Recuperar a tag NFC da intenção
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)

            // Verificar se a tag e os dados escaneados não são nulos
            tag?.let {
                if (scannedData != null) {
                    // Escrever os dados escaneados na tag NFC
                    writeTagData(tag, scannedData!!)
                } else {
                    // Mostrar uma mensagem se a tag NFC não foi detectada ou se os dados a serem gravados estão ausentes
                    Toast.makeText(this, "Nenhuma tag NFC detectada ou dados a gravar estão ausentes", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Método para escrever dados em uma tag NFC
    private fun writeTagData(tag: Tag?, data: String) {
        if (tag == null) return

        // Criar uma mensagem NDEF com os dados
        val ndefMessage = NdefMessage(NdefRecord.createTextRecord(null, data))

        // Obter uma instância de NDEF para a tag e conectar-se a ela
        val ndef = Ndef.get(tag)
        ndef?.connect()
        // Escrever a mensagem NDEF na tag
        ndef?.writeNdefMessage(ndefMessage)
        // Desconectar da tag
        ndef?.close()

        // Mostrar uma mensagem indicando que os dados foram gravados com sucesso na tag NFC
        Toast.makeText(this, "Dados gravados na tag NFC com sucesso", Toast.LENGTH_SHORT).show()
    }

    // Método chamado quando o botão de voltar é pressionado
    override fun onBackPressed() {
        super.onBackPressed()
        // Iniciar a atividade principal do gerente
        val intent = Intent(applicationContext, ManagerMainActivity::class.java)
        startActivity(intent)
        finish()
    }
}