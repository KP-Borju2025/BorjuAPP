package com.kp.borju_kp.admin

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.FirebaseFirestore
import com.kp.borju_kp.R
import com.kp.borju_kp.admin.adapter.DetailPesananAdapter
import com.kp.borju_kp.data.Order
import java.text.SimpleDateFormat
import java.util.Locale

class DetailPesananActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var customerName: TextView
    private lateinit var orderType: TextView
    private lateinit var paymentMethod: TextView
    private lateinit var currentStatus: TextView
    private lateinit var orderTimestamp: TextView
    private lateinit var rvItems: RecyclerView
    private lateinit var totalPrice: TextView
    private lateinit var actvStatusSelector: AutoCompleteTextView
    private lateinit var btnUpdateStatus: Button

    private val db = FirebaseFirestore.getInstance()
    private var orderId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_pesanan)
        enableEdgeToEdge()

        // Inisialisasi Views
        toolbar = findViewById(R.id.toolbar_detail_pesanan)
        customerName = findViewById(R.id.tv_detail_customer_name)
        orderType = findViewById(R.id.tv_detail_order_type)
        paymentMethod = findViewById(R.id.tv_detail_payment_method)
        currentStatus = findViewById(R.id.tv_detail_current_status)
        orderTimestamp = findViewById(R.id.tv_detail_order_timestamp)
        rvItems = findViewById(R.id.rv_detail_pesanan_items)
        totalPrice = findViewById(R.id.tv_detail_total_price)
        actvStatusSelector = findViewById(R.id.actv_status_selector)
        btnUpdateStatus = findViewById(R.id.btn_update_status)

        setupToolbar()
        setupStatusDropdown()

        orderId = intent.getStringExtra("ORDER_ID")
        if (orderId == null) {
            showErrorAndFinish("ID Pesanan tidak valid")
            return
        }

        fetchOrderDetails(orderId!!)

        btnUpdateStatus.setOnClickListener {
            updateOrderStatus()
        }
    }

    private fun updateOrderStatus() {
        val newStatus = actvStatusSelector.text.toString()
        if (newStatus.isEmpty()) {
            Toast.makeText(this, "Pilih status baru terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        orderId?.let { id ->
            db.collection("orders").document(id)
                .update("status", newStatus)
                .addOnSuccessListener {
                    Toast.makeText(this, "Status pesanan berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    fetchOrderDetails(id) // <-- PERBAIKAN: Menggunakan ID yang sudah ada
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal memperbarui status: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchOrderDetails(id: String) {
        db.collection("orders").document(id).get().addOnSuccessListener {
            val order = it.toObject(Order::class.java)
            if (order != null) {
                displayOrderDetails(order)
            } else {
                showErrorAndFinish("Gagal membaca data pesanan")
            }
        }.addOnFailureListener { 
            showErrorAndFinish("Gagal mengambil data pesanan")
        }
    }

    private fun displayOrderDetails(order: Order) {
        customerName.text = "${order.customerName}"
        orderType.text = "${order.orderType}"
        paymentMethod.text = "${order.paymentMethod}"
        currentStatus.text = "${order.status}"
        totalPrice.text = "Total: Rp ${order.totalPrice.toInt()}"
        
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        orderTimestamp.text = "${order.orderTimestamp?.let { sdf.format(it) } ?: "-"}"
        
        // Set status saat ini di dropdown
        actvStatusSelector.setText(order.status, false)

        rvItems.layoutManager = LinearLayoutManager(this)
        rvItems.adapter = DetailPesananAdapter(order.items)
    }

    private fun setupStatusDropdown() {
        val statuses = resources.getStringArray(R.array.order_statuses)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statuses)
        actvStatusSelector.setAdapter(adapter)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }
    
    private fun showErrorAndFinish(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }
}