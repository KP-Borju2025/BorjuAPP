package com.kp.borju_kp.admin

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.kp.borju_kp.R
import com.kp.borju_kp.admin.adapter.CheckoutAdapter
import com.kp.borju_kp.data.Order
import com.kp.borju_kp.data.OrderItem

class CheckoutActivity : AppCompatActivity(), CheckoutAdapter.OnCartUpdateListener {

    private lateinit var rvCheckoutItems: RecyclerView
    private lateinit var tvFinalTotalPrice: TextView
    private lateinit var btnProcessPayment: Button
    private lateinit var toolbar: MaterialToolbar
    private lateinit var etCustomerName: TextInputEditText
    private lateinit var actvPaymentMethod: AutoCompleteTextView
    
    private lateinit var checkoutAdapter: CheckoutAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        rvCheckoutItems = findViewById(R.id.rv_checkout_items)
        tvFinalTotalPrice = findViewById(R.id.tv_final_total_price)
        btnProcessPayment = findViewById(R.id.btn_process_payment)
        toolbar = findViewById(R.id.toolbar_checkout)
        etCustomerName = findViewById(R.id.et_customer_name)
        actvPaymentMethod = findViewById(R.id.actv_payment_method)

        setupToolbar()
        setupPaymentMethodDropdown()
        setupRecyclerView()
        updateSummary()

        btnProcessPayment.setOnClickListener { processOrder() }
    }

    override fun onCartUpdated() {
        updateSummary()
        checkoutAdapter.notifyDataSetChanged()
        if (CartManager.getCartItems().isEmpty()) {
            Toast.makeText(this, "Keranjang kosong", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun processOrder() {
        val customerName = etCustomerName.text.toString().trim()
        val paymentMethod = actvPaymentMethod.text.toString()

        if (customerName.isEmpty()) {
            etCustomerName.error = "Nama tidak boleh kosong"
            return
        }
        if (paymentMethod.isEmpty()) {
            Toast.makeText(this, "Pilih metode pembayaran", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(this).apply { setMessage("Menyimpan pesanan..."); setCancelable(false); show() }

        val orderItems = CartManager.getCartItems().map { OrderItem(it.menu.id, it.menu.name, it.menu.price, it.quantity, it.note) }
        
        // PERBAIKAN: Menggunakan named arguments untuk menghindari error urutan
        val order = Order(
            customerName = customerName,
            paymentMethod = paymentMethod,
            totalPrice = CartManager.getTotalPrice(),
            status = "Selesai",
            orderType = "Offline",
            orderTimestamp = null, // Akan diisi oleh server
            items = orderItems
        )

        // --- LOGIKA UPDATE STOK DENGAN WRITE BATCH ---
        val batch = db.batch()

        val newOrderRef = db.collection("orders").document()
        batch.set(newOrderRef, order)

        CartManager.getCartItems().forEach { cartItem ->
            val menuRef = db.collection("menus").document(cartItem.menu.id)
            val stockToDecrement = -cartItem.quantity.toLong()
            batch.update(menuRef, "stok", FieldValue.increment(stockToDecrement))
        }

        batch.commit().addOnSuccessListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Pesanan berhasil disimpan!", Toast.LENGTH_LONG).show()
            CartManager.clearCart()
            val intent = Intent(this, KasirActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            finish()
        }.addOnFailureListener { e ->
            progressDialog.dismiss()
            Log.e("CheckoutActivity", "Error committing batch", e)
            Toast.makeText(this, "Gagal menyimpan pesanan: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecyclerView() {
        checkoutAdapter = CheckoutAdapter(CartManager.getCartItems(), this)
        rvCheckoutItems.layoutManager = LinearLayoutManager(this)
        rvCheckoutItems.adapter = checkoutAdapter
    }

    private fun updateSummary() {
        tvFinalTotalPrice.text = "Rp ${CartManager.getTotalPrice().toInt()}"
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupPaymentMethodDropdown() {
        val paymentMethods = resources.getStringArray(R.array.payment_methods)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, paymentMethods)
        actvPaymentMethod.setAdapter(adapter)
    }
}