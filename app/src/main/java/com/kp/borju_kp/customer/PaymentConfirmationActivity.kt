package com.kp.borju_kp.customer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.kp.borju_kp.CloudinaryConfig
import com.kp.borju_kp.R
import com.kp.borju_kp.data.CartItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaymentConfirmationActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var tvPaymentTotal: TextView
    private lateinit var ivQrisCode: ImageView
    private lateinit var tvBankAccount: TextView
    private lateinit var ivProofPreview: ImageView
    private lateinit var btnUploadProof: Button
    private lateinit var btnConfirmPayment: Button

    private var orderData: HashMap<String, Any>? = null
    private var proofImageUri: Uri? = null

    private val db = FirebaseFirestore.getInstance()

    private companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_confirmation)

        toolbar = findViewById(R.id.toolbar)
        tvPaymentTotal = findViewById(R.id.tv_payment_total)
        ivQrisCode = findViewById(R.id.iv_qris_code)
        tvBankAccount = findViewById(R.id.tv_bank_account)
        ivProofPreview = findViewById(R.id.iv_proof_preview)
        btnUploadProof = findViewById(R.id.btn_upload_proof)
        btnConfirmPayment = findViewById(R.id.btn_confirm_payment)

        setupToolbar()

        // Ambil data pesanan dari intent
        orderData = intent.getSerializableExtra("order_data") as? HashMap<String, Any>
        val paymentMethod = orderData?.get("paymentMethod") as? String
        val totalPrice = orderData?.get("totalPrice") as? Double

        if (orderData == null || paymentMethod == null || totalPrice == null) {
            Toast.makeText(this, "Data pesanan tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvPaymentTotal.text = "Rp ${totalPrice.toInt()}"

        // Tampilkan metode pembayaran yang sesuai
        when (paymentMethod) {
            "QRIS" -> {
                ivQrisCode.visibility = View.VISIBLE
                // TODO: Ganti dengan gambar QRIS Anda dari drawable
                ivQrisCode.setImageResource(R.drawable.ic_launcher_foreground)
            }
            "Transfer Bank" -> {
                tvBankAccount.visibility = View.VISIBLE
                // TODO: Isi dengan nomor rekening Anda
                tvBankAccount.text = "Bank BCA\n123-456-7890\na.n. Kedai Kopi Borju"
            }
        }

        btnUploadProof.setOnClickListener {
            openFileChooser()
        }

        btnConfirmPayment.setOnClickListener {
            if (proofImageUri == null) {
                Toast.makeText(this, "Harap unggah bukti pembayaran", Toast.LENGTH_SHORT).show()
            } else {
                uploadProofAndPlaceOrder()
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            proofImageUri = data.data
            ivProofPreview.setImageURI(proofImageUri)
        }
    }

    private fun uploadProofAndPlaceOrder() {
        btnConfirmPayment.isEnabled = false
        Toast.makeText(this, "Mengunggah bukti & membuat pesanan...", Toast.LENGTH_LONG).show()

        proofImageUri?.let { uri ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val uploadResult = CloudinaryConfig.instance.uploader().upload(inputStream?.readBytes(), null)
                    val proofImageUrl = uploadResult["secure_url"] as? String

                    withContext(Dispatchers.Main) {
                        if (proofImageUrl != null) {
                            saveOrderToFirestore(proofImageUrl)
                        } else {
                            showErrorAndEnableButton()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PaymentConfirmation", "Upload failed", e)
                    withContext(Dispatchers.Main) {
                        showErrorAndEnableButton()
                    }
                }
            }
        }
    }

    private fun saveOrderToFirestore(proofImageUrl: String) {
        orderData?.put("paymentProofUrl", proofImageUrl)

        db.collection("orders").add(orderData!!)
            .addOnSuccessListener {
                Toast.makeText(this, "Pesanan berhasil dibuat!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DashboardCostumer::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                showErrorAndEnableButton("Gagal menyimpan pesanan: ${e.message}")
            }
    }

    private fun showErrorAndEnableButton(message: String = "Gagal memproses pesanan") {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        btnConfirmPayment.isEnabled = true
    }
}