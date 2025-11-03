package com.kp.borju_kp.customer

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.kp.borju_kp.R
import com.kp.borju_kp.customer.adapter.CheckoutAdapter
import com.kp.borju_kp.data.CartItem
import com.kp.borju_kp.utils.SessionManager

class CheckoutActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var rvCheckoutItems: RecyclerView
    private lateinit var tvTotalPrice: TextView
    private lateinit var rgPaymentMethod: RadioGroup
    private lateinit var btnPlaceOrder: Button

    private var cartItems: ArrayList<CartItem>? = null
    private var totalPrice: Double = 0.0

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        toolbar = findViewById(R.id.toolbar)
        rvCheckoutItems = findViewById(R.id.rv_checkout_items)
        tvTotalPrice = findViewById(R.id.tv_checkout_total_price)
        rgPaymentMethod = findViewById(R.id.rg_payment_method)
        btnPlaceOrder = findViewById(R.id.btn_place_order)

        setupToolbar()

        // Ambil data dari Intent
        cartItems = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("cart_items", CartItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra("cart_items")
        }
        totalPrice = intent.getDoubleExtra("total_price", 0.0)

        if (cartItems != null) {
            setupRecyclerView()
            tvTotalPrice.text = "Rp ${totalPrice.toInt()}"
        } else {
            Toast.makeText(this, "Keranjang kosong!", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnPlaceOrder.setOnClickListener {
            placeOrder()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupRecyclerView() {
        rvCheckoutItems.layoutManager = LinearLayoutManager(this)
        rvCheckoutItems.adapter = CheckoutAdapter(cartItems!!)
    }

    private fun placeOrder() {
        val userId = SessionManager.getUserId()
        val customerName = "Nama Customer" // TODO: Ambil nama customer dari SessionManager/Firestore
        val selectedPaymentMethodId = rgPaymentMethod.checkedRadioButtonId
        val paymentMethod = if (selectedPaymentMethodId == R.id.rb_qris) "QRIS" else "Cash"

        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "Anda harus login untuk membuat pesanan", Toast.LENGTH_SHORT).show()
            return
        }

        val itemsForFirestore = cartItems?.map { cartItem ->
            mapOf(
                "menuId" to cartItem.menu.id,
                "menuName" to cartItem.menu.name,
                "quantity" to cartItem.quantity,
                "price" to cartItem.menu.price
            )
        }

        val orderData = hashMapOf(
            "customerId" to userId,
            "customerName" to customerName,
            "items" to itemsForFirestore,
            "orderTimestamp" to Timestamp.now(),
            "totalPrice" to totalPrice,
            "paymentMethod" to paymentMethod,
            "orderType" to "Online", // atau tipe lain sesuai kebutuhan
            "status" to "Menunggu Konfirmasi" 
        )

        db.collection("orders")
            .add(orderData)
            .addOnSuccessListener {
                Toast.makeText(this, "Pesanan berhasil dibuat!", Toast.LENGTH_SHORT).show()
                // TODO: Kosongkan keranjang di ViewModel dan kembali ke halaman utama
                finish()
            }
            .addOnFailureListener { e ->
                Log.w("CheckoutActivity", "Error adding document", e)
                Toast.makeText(this, "Gagal membuat pesanan", Toast.LENGTH_SHORT).show()
            }
    }
}