package com.kp.borju_kp.admin

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.kp.borju_kp.R

class DashboardAdmin : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_admin)

        loadStatistics()
        setupNavigation()
    }

    private fun loadStatistics() {
        val tvJumlahPesanan: TextView = findViewById(R.id.tv_jumlah_pesanan)
        val tvPemasukan: TextView = findViewById(R.id.tv_pemasukan)
        val tvPengeluaran: TextView = findViewById(R.id.tv_pengeluaran)

        tvJumlahPesanan.text = "15"
        tvPemasukan.text = "Rp 450K"
        tvPengeluaran.text = "Rp 120K"
    }

    private fun setupNavigation() {
        findViewById<MaterialCardView>(R.id.card_pos).setOnClickListener {
            // Ganti PosActivity::class.java dengan Activity Kasir Anda
            // startActivity(Intent(this, PosActivity::class.java))
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
            // TODO: Buat RiwayatPesananAdminActivity dan ganti di sini
            // startActivity(Intent(this, RiwayatPesananAdminActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_user).setOnClickListener {
            // TODO: Buat ManajemenUserActivity dan ganti di sini
             startActivity(Intent(this, ManajemenUserActivity::class.java))
        }
    }
}