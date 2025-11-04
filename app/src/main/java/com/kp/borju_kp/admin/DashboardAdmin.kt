package com.kp.borju_kp.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.kp.borju_kp.R
import com.kp.borju_kp.admin.ProfileAdminActivity
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class DashboardAdmin : AppCompatActivity() {

    private lateinit var tvJumlahPesanan: TextView
    private lateinit var tvLabaBersih: TextView
    private lateinit var tvTotalPengeluaran: TextView

    private val db = FirebaseFirestore.getInstance()
    private var ordersListener: ListenerRegistration? = null
    private var expensesListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_admin)

        tvJumlahPesanan = findViewById(R.id.tv_jumlah_pesanan_value)
        tvLabaBersih = findViewById(R.id.tv_laba_bersih_value)
        tvTotalPengeluaran = findViewById(R.id.tv_pengeluaran_value)

        setupNavigation()
    }

    override fun onStart() {
        super.onStart()
        listenToTodaysStats() // PERBAIKAN: Menggunakan fungsi yang baru
    }

    override fun onStop() {
        super.onStop()
        ordersListener?.remove()
        expensesListener?.remove()
    }

    private fun listenToTodaysStats() {
        // 1. Tentukan rentang waktu untuk "Hari Ini"
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0)
        val startOfDay = calendar.time

        calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59); calendar.set(Calendar.SECOND, 59)
        val endOfDay = calendar.time
        
        var todaysRevenue = 0.0
        var todaysExpenses = 0.0

        // 2. Listener untuk Pesanan (Orders) HARI INI
        ordersListener = db.collection("orders")
            .whereEqualTo("status", "Selesai")
            .whereGreaterThanOrEqualTo("orderTimestamp", startOfDay)
            .whereLessThanOrEqualTo("orderTimestamp", endOfDay)
            .addSnapshotListener { snapshots, error ->
                if (error != null) { Log.w("DashboardAdmin", "Listen error on today's orders", error); return@addSnapshotListener }
                todaysRevenue = snapshots?.sumOf { it.getDouble("totalPrice") ?: 0.0 } ?: 0.0
                tvJumlahPesanan.text = (snapshots?.size() ?: 0).toString()
                updateNetProfit(todaysRevenue, todaysExpenses)
            }

        // 3. Listener untuk Pengeluaran (Expenses) HARI INI
        expensesListener = db.collection("pengeluaran")
            .whereGreaterThanOrEqualTo("tanggal", startOfDay)
            .whereLessThanOrEqualTo("tanggal", endOfDay)
            .addSnapshotListener { snapshots, error ->
                if (error != null) { Log.w("DashboardAdmin", "Listen error on today's expenses", error); return@addSnapshotListener }
                todaysExpenses = snapshots?.sumOf { it.getDouble("jumlah") ?: 0.0 } ?: 0.0
                tvTotalPengeluaran.text = formatCurrency(todaysExpenses)
                updateNetProfit(todaysRevenue, todaysExpenses)
            }
    }

    private fun updateNetProfit(revenue: Double, expenses: Double) {
        tvLabaBersih.text = formatCurrency(revenue - expenses)
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }

    private fun setupNavigation() {
        findViewById<ImageButton>(R.id.btn_profile).setOnClickListener {
            startActivity(Intent(this, ProfileAdminActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.card_pos).setOnClickListener {
            startActivity(Intent(this, KasirActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.card_laporan).setOnClickListener {
            startActivity(Intent(this, LaporanActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.card_menu).setOnClickListener {
            startActivity(Intent(this, ManajemenMenuActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.card_pengeluaran).setOnClickListener {
            startActivity(Intent(this, ManajemenPengeluaranActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.card_riwayat_pesanan).setOnClickListener {
             startActivity(Intent(this, RiwayatPesananActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.card_user).setOnClickListener {
             startActivity(Intent(this, ManajemenUserActivity::class.java))
        }
    }
}