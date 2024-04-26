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
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder

class MainActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var buttonLogout: Button
    private lateinit var btn_cad_cartao: TextView
    private lateinit var btn_continue: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        buttonLogout = findViewById(R.id.btn_logout)
        btn_cad_cartao = findViewById(R.id.btn_cad_cartao)
        btn_continue = findViewById(R.id.btn_prosseguir)

        buttonLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
            finish()
        }

        val userId = auth.currentUser?.uid
        if (userId != null) {
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

            firestore.collection("locacoes")
                .whereEqualTo("usuarioId", userId)
                .whereEqualTo("ativo", false)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        showQRCodeAlert(documents.documents.first())
                    }
                }
        }

        btn_cad_cartao.setOnClickListener {
            val intent = Intent(applicationContext, RegisterCreditCard::class.java)
            startActivity(intent)
            finish()
        }

        btn_continue.setOnClickListener {
            val intent = Intent(applicationContext, MapsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showQRCodeAlert(document: DocumentSnapshot) {
        val dialogView = layoutInflater.inflate(R.layout.qr_code_alert, null)
        val qrCodeImageView = dialogView.findViewById<ImageView>(R.id.qrCodeImageView)
        val data = document.data.toString()
        val qrCodeBitmap = generateQRCode(data)
        qrCodeImageView.setImageBitmap(qrCodeBitmap)

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setNegativeButton("Cancelar") { dialog, which ->
            removeLocacaoDocument(document.id)
            dialog.dismiss()
        }
        builder.show()
    }

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

    private fun generateQRCode(data: String): Bitmap? {
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(data, BarcodeFormat.QR_CODE, 800, 800)
            val barcodeEncoder = BarcodeEncoder()
            return barcodeEncoder.createBitmap(bitMatrix)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
