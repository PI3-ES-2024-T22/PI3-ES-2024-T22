package com.example.pi3_es_2024_t22

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.journeyapps.barcodescanner.BarcodeEncoder

class MainActivity : AppCompatActivity() {

    // Declarando instâncias de Firebase e elementos de interface
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var buttonLogout: Button
    private lateinit var btn_cad_cartao: TextView
    private lateinit var btn_continue: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializando instâncias de Firebase e elementos de interface
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        buttonLogout = findViewById(R.id.btn_logout)
        btn_cad_cartao = findViewById(R.id.btn_cad_cartao)
        btn_continue = findViewById(R.id.btn_prosseguir)

        // Configurando listener para o botão de logout
        buttonLogout.setOnClickListener {
            // Faz logout do usuário e redireciona para a tela de login
            auth.signOut()
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
            finish()
        }

        // Verificando se há um usuário logado
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Verificando se o usuário possui um cartão cadastrado
            firestore.collection("Pessoas").document(userId).get()
                .addOnSuccessListener { documentSnapshot ->
                    val cartao = documentSnapshot.get("Cartao") as? Map<String, Any>
                    if (cartao != null && cartao.isNotEmpty()) {
                        btn_cad_cartao.isEnabled = false
                        btn_cad_cartao.text = "Cartão já cadastrado!"
                    } else {
                        btn_cad_cartao.visibility = TextView.VISIBLE
                    }
                }

            // Verificando se há locações ativas para o usuário
            firestore.collection("locacoes")
                .whereEqualTo("usuarioId", userId)
                .whereEqualTo("ativo", false)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        // Se houver locações ativas, exibe um alerta com o código QR
                        showQRCodeAlert(documents.documents.first())
                    }
                }
        }

        // Configurando listener para o botão de cadastrar cartão
        btn_cad_cartao.setOnClickListener {
            // Redireciona para a tela de cadastro de cartão
            val intent = Intent(applicationContext, RegisterCreditCard::class.java)
            startActivity(intent)
            finish()
        }

        // Configurando listener para o botão de prosseguir
        btn_continue.setOnClickListener {
            // Redireciona para a tela de mapa
            val intent = Intent(applicationContext, MapsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Função para exibir um alerta com o código QR
    private fun showQRCodeAlert(document: DocumentSnapshot) {
        val dialogView = layoutInflater.inflate(R.layout.qr_code_alert, null)
        val qrCodeImageView = dialogView.findViewById<ImageView>(R.id.qrCodeImageView)
        val data = document.data.toString()
        val qrCodeBitmap = generateQRCode(document.id)
        qrCodeImageView.setImageBitmap(qrCodeBitmap)

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setNegativeButton("Cancelar") { dialog, which ->
            removeLocacaoDocument(document.id)
            dialog.dismiss()
        }
        builder.show()
    }

    // Função para remover um documento da coleção "locacoes" no Firestore
    private fun removeLocacaoDocument(documentId: String) {
        firestore.collection("locacoes").document(documentId)
            .delete()
            .addOnSuccessListener {
                Log.i("Firebase", "Document deleted with ID: $documentId")
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "Error deleting document: $e")
            }
    }

    // Função para gerar um código QR a partir dos dados fornecidos
    private fun generateQRCode(id: String): Bitmap {
        val charset = Charsets.UTF_8
        val byteArray = id.toByteArray(charset)

        val hints = mapOf<EncodeHintType, ErrorCorrectionLevel>(Pair(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H))
        val matrix = MultiFormatWriter().encode(String(byteArray, charset), BarcodeFormat.QR_CODE, 300, 300, hints)
        val barcodeEncoder = BarcodeEncoder()
        return barcodeEncoder.createBitmap(matrix)
    }
}