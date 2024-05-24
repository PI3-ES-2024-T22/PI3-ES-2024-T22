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
import org.json.JSONException
import org.json.JSONObject
import java.util.Date

class DiscoverTagActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var tagDataTextView: TextView
    private lateinit var writeButton: Button
    private lateinit var clientImage1: ImageView
    private lateinit var clientImage2: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var clienteInfo: TextView
    private lateinit var prosseguirButton: Button
    private lateinit var encerrarButton: Button
    private lateinit var abrirArmarioButton: Button
    private lateinit var encerramentoTextView: TextView

    private var scannedData: String? = null
    private var tag: Tag? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover_tag)

        auth = FirebaseAuth.getInstance()
        tagDataTextView = findViewById(R.id.tagDataTextView)
        clienteInfo = findViewById(R.id.tagClienteInfo)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        writeButton = findViewById(R.id.writeButton)
        clientImage1 = findViewById(R.id.tagDataImage1)
        clientImage2 = findViewById(R.id.tagDataImage2)
        db = FirebaseFirestore.getInstance()
        prosseguirButton = findViewById(R.id.prosseguirButton)
        encerrarButton = findViewById(R.id.encerrarButton)
        abrirArmarioButton = findViewById(R.id.abrirArmarioButton)
        encerramentoTextView = findViewById(R.id.EncerrarTextoCliente)

        scannedData = intent.getStringExtra("scannedData")

        val scannedDataJson = JSONObject(scannedData)
        val locacaoId = scannedDataJson.getString("locacaoId")

        fetchFirestoreData()

        writeButton.visibility = View.VISIBLE


        writeButton.setOnClickListener {
            writeButton.visibility = View.GONE
            prosseguirButton.visibility = View.VISIBLE

            try {
                writeTagData(tag)
            } catch (e: Exception) {
                Toast.makeText(this, "Mantenha a TAG próxima ao celular", Toast.LENGTH_SHORT).show()
            }
        }

        prosseguirButton.setOnClickListener {
            encerrarButton.visibility = View.VISIBLE
            abrirArmarioButton.visibility = View.VISIBLE
            prosseguirButton.visibility = View.GONE
        }

        abrirArmarioButton.setOnClickListener {
            // Lógica para abrir armário
        }

        encerrarButton.setOnClickListener {
            try {
                eraseTagData(tag)
            } catch (e: Exception) {
                Toast.makeText(this, "Mantenha a TAG próxima ao celular", Toast.LENGTH_SHORT).show()
            }

            val tempoFimLocacao = Date()
            db.collection("locacoes").document(locacaoId).get()
                .addOnSuccessListener { document ->
                    val tempoInicioLocacaoTimestamp = document.getTimestamp("tempoInicioLocacao")
                    val tempoInicioLocacao = tempoInicioLocacaoTimestamp?.toDate()

                    val caucao = calcularCaucao(tempoInicioLocacao, tempoFimLocacao)
                    val userMap = hashMapOf(
                        "EstornoCaucao" to caucao
                    )

                    db.collection("locacoes").document(locacaoId).update(userMap as Map<String, Any>)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Locação encerrada com sucesso.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Erro ao encerrar a locação.", Toast.LENGTH_SHORT).show()
                        }
                }
        }


    }

    //ENIGMA -> por algum motivo fetchFirestoreData() so funciona no onCreate mas nao no onNewIntent
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent) // Adicione esta linha

        if (intent?.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            tag?.let {
                writeButton.visibility = View.GONE // Mova esta linha para aqui
                clientImage1.visibility = View.VISIBLE
                clientImage2.visibility = View.VISIBLE
                tagDataTextView.text = "O usuário a alocar o armário:"

                scannedData = intent.getStringExtra("scannedData")
            }
        }
    }

    private fun fetchFirestoreData() {
        scannedData = intent.getStringExtra("scannedData")
        Log.d("ScannedData", "Valor: $scannedData")

        try {
            val scannedDataJson = JSONObject(scannedData)
            val locacaoId = scannedDataJson.getString("locacaoId")
            Log.d("DiscoverTagActivity", "Valor de locacaoId: $locacaoId")

            db.collection("locacoes").document(locacaoId).get()
                .addOnSuccessListener { document ->
                    scannedData = intent.getStringExtra("scannedData")

                    Log.d("DiscoverTagActivity", "Document data: ${document.data}")
                    Log.d("DiscoverTagActivity", "Document exists: ${document?.exists()}")

                    val ativo = document.getBoolean("ativo") ?: false
                    val localId = document.getString("localId") ?: ""
                    val preco = document.getString("preco") ?: "0"
                    val usuarioId = document.getString("usuarioId") ?: ""
                    val clientPhotoLink1 = document.getString("imagelink1") ?: ""
                    val clientPhotoLink2 = document.getString("imagelink2") ?: ""
                    val locacaoID = document.id

                    try {
                        Glide.with(this@DiscoverTagActivity).load(clientPhotoLink1).into(clientImage1)
                        Glide.with(this@DiscoverTagActivity).load(clientPhotoLink2).into(clientImage2)
                    } catch (e: Exception) {
                        Log.e("DiscoverTagActivity", "Erro ao carregar a imagem", e)
                    }

                    val userData = "Imagem1: $clientPhotoLink1\nImagem2: $clientPhotoLink2\nPreço: $preco\nAtivo: $ativo\nLocalId: $localId\nUsuarioId: $usuarioId\nLocacaoId: $locacaoID"
                    clienteInfo.text = userData
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this@DiscoverTagActivity, "Erro ao acessar o Firestore", Toast.LENGTH_SHORT).show()
                    Log.e("DiscoverTagActivity", "Erro ao acessar o Firestore", exception)
                }
        } catch (e: JSONException) {
            Log.e("DiscoverTagActivity", "Erro ao analisar o JSON", e)
        }

    }

    override fun onResume() {
        super.onResume()

        scannedData = intent.getStringExtra("scannedData")
        Log.d("onResume", "Chegamos onResume com scannedData: $scannedData")

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

    private fun writeTagData(tag: Tag?) {
        if (tag == null) return

        //Salvar apenas ID
        db.collection("locacoes").document(scannedData!!).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val localId = document.getString("localId") ?: ""
                    val preco = document.getString("preco") ?: "0"
                    val clientPhotoLink1 = document.getString("imagelink1")
                    val clientPhotoLink2 = document.getString("imagelink2")
                    val usuarioId = document.getString("usuarioId") ?: ""

                    val userData = "Local: $localId\nPreço: $preco\nImagem1: $clientPhotoLink1\nImagem2: $clientPhotoLink2\nUsuarioId: $usuarioId"
                    val newNdefMessage = NdefMessage(NdefRecord.createTextRecord(null, userData))

                    val ndef = Ndef.get(tag)
                    ndef?.connect()
                    ndef?.writeNdefMessage(newNdefMessage)
                    ndef?.close()

                    Toast.makeText(this, "Informações do usuário foram gravadas na tag NFC com sucesso.", Toast.LENGTH_SHORT).show()
                }
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
}

