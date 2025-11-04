package com.kp.borju_kp.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.kp.borju_kp.R
import com.kp.borju_kp.admin.adapter.RiwayatPesananAdapter
import com.kp.borju_kp.data.Order

class RiwayatPesananActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var rvRiwayat: RecyclerView
    private lateinit var riwayatAdapter: RiwayatPesananAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyMessage: TextView

    private val db = FirebaseFirestore.getInstance()
    private var ordersListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat_pesanan)

        toolbar = findViewById(R.id.toolbar_riwayat)
        rvRiwayat = findViewById(R.id.rv_riwayat_pesanan)
        progressBar = findViewById(R.id.progress_bar)
        tvEmptyMessage = findViewById(R.id.tv_empty_message)

        setupToolbar()
        setupRecyclerView()
    }

    override fun onStart() {
        super.onStart()
        listenToOrders()
    }

    override fun onStop() {
        super.onStop()
        ordersListener?.remove()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupRecyclerView() {
        riwayatAdapter = RiwayatPesananAdapter(
            onCardClick = { orderId ->
                val intent = Intent(this, DetailPesananActivity::class.java)
                intent.putExtra("ORDER_ID", orderId)
                startActivity(intent)
            },
            onStatusClick = { order ->
                showStatusChangeDialog(order)
            }
        )
        rvRiwayat.layoutManager = LinearLayoutManager(this)
        rvRiwayat.adapter = riwayatAdapter
    }

    private fun showStatusChangeDialog(order: Order) {
        val statuses = resources.getStringArray(R.array.order_statuses)
        MaterialAlertDialogBuilder(this)
            .setTitle("Ubah Status Pesanan")
            .setItems(statuses) { dialog, which ->
                val newStatus = statuses[which]
                if (newStatus != order.status) {
                    // PERBAIKAN: Mengirim seluruh objek 'order' bukan hanya ID
                    updateOrderStatus(order, newStatus)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun updateOrderStatus(order: Order, newStatus: String) {
        // Jika status baru adalah "Selesai", kurangi stok. Jika tidak, hanya ubah status.
        if (newStatus == "Selesai") {
            updateStockAndStatus(order, newStatus)
        } else {
            db.collection("orders").document(order.id)
                .update("status", newStatus)
                .addOnSuccessListener {
                    Toast.makeText(this, "Status berhasil diubah ke $newStatus", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal mengubah status: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateStockAndStatus(order: Order, newStatus: String) {
        val batch = db.batch()

        // 1. Perbarui status pesanan
        val orderRef = db.collection("orders").document(order.id)
        batch.update(orderRef, "status", newStatus)

        // 2. Kurangi stok untuk setiap item dalam pesanan
        for (item in order.items) {
            val menuRef = db.collection("menus").document(item.menuId)
            val decrement = -item.quantity.toLong()
            batch.update(menuRef, "stok", FieldValue.increment(decrement))
        }

        // 3. Jalankan semua operasi sekaligus
        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(this, "Status diubah & stok diperbarui!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memperbarui: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun listenToOrders() {
        progressBar.visibility = View.VISIBLE
        tvEmptyMessage.visibility = View.GONE
        rvRiwayat.visibility = View.GONE

        val query = db.collection("orders").orderBy("orderTimestamp", Query.Direction.DESCENDING)

        ordersListener = query.addSnapshotListener { snapshots, error ->
            progressBar.visibility = View.GONE
            if (error != null) {
                Log.e("RiwayatPesanan", "Listen failed.", error)
                return@addSnapshotListener
            }

            if (snapshots != null && !snapshots.isEmpty) {
                val orderList = snapshots.documents.mapNotNull {
                    it.toObject(Order::class.java)?.apply { id = it.id }
                }
                riwayatAdapter.submitList(orderList)
                rvRiwayat.visibility = View.VISIBLE
            } else {
                tvEmptyMessage.visibility = View.VISIBLE
                riwayatAdapter.submitList(emptyList())
            }
        }
    }
}
