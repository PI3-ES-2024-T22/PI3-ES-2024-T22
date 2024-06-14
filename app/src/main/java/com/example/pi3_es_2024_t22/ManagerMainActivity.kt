package com.example.pi3_es_2024_t22

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.integration.android.IntentIntegrator

// Classe principal do gerente
class ManagerMainActivity : AppCompatActivity() {

    // Declaração de variáveis
    private lateinit var auth: FirebaseAuth

    // Solicitação de permissão de câmera
    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Se a permissão for concedida, inicie o scanner QR
            startQRScanner()
        } else {
            // Lidar com a negação da permissão
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manager_main)

        // Configuração de janela
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialização do FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Configuração do botão de escaneamento QR
        findViewById<Button>(R.id.btnScanQR).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                // Se a permissão da câmera não for concedida, solicite a permissão
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            } else {
                // Se a permissão da câmera for concedida, inicie o scanner QR
                startQRScanner()
            }
        }

        // Configuração do botão de logout
        findViewById<Button>(R.id.btn_logout).setOnClickListener {
            // Fazer logout do usuário
            auth.signOut()
            // Iniciar a atividade de login
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
            finish()
        }

        // Configuração do botão de abertura do armário
        findViewById<Button>(R.id.btnOpenLocker).setOnClickListener {
            // Iniciar a atividade de escaneamento NFC
            val intent = Intent(applicationContext, NfcScannerActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Função para iniciar o scanner QR
    private fun startQRScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Escaneie o código QR para liberar a locação")
        integrator.setCameraId(0) // Use a câmera específica do dispositivo
        integrator.setBeepEnabled(false)
        integrator.setBarcodeImageEnabled(true)
        integrator.initiateScan()
    }

    // Função para lidar com o resultado da atividade de escaneamento QR
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                // Lidar com o cancelamento
            } else {
                // Se o escaneamento foi bem-sucedido, iniciar a atividade de liberação de localização
                val scannedData = result.contents
                val intent = Intent(this, ReleaseLocationActivity::class.java)
                intent.putExtra("scannedData", scannedData)
                startActivity(intent)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}