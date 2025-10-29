package com.kp.borju_kp.admin

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.kp.borju_kp.R

class DashboardAdmin : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_admin)
        enableEdgeToEdge()

        // Muat data statistik
        loadStatistics()

        // Siapkan navigasi menu
        setupNavigation()
    }

    private fun loadStatistics() {
        val tvJumlahPesanan: TextView = findViewById(R.id.tv_jumlah_pesanan)
        val tvPemasukan: TextView = findViewById(R.id.tv_pemasukan)
        val tvPengeluaran: TextView = findViewById(R.id.tv_pengeluaran)

        // TODO: Ganti dengan data asli dari Firebase/Database
        // Untuk saat ini, kita gunakan data dummy.
        tvJumlahPesanan.text = "15"
        tvPemasukan.text = "Rp 450K"
        tvPengeluaran.text = "Rp 120K"
    }

    private fun setupNavigation() {
        findViewById<MaterialCardView>(R.id.card_pos).setOnClickListener {
            // Ganti PosActivity::class.java dengan Activity Kasir Anda
            startActivity(Intent(this, KasirActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_laporan).setOnClickListener {
            // TODO: Buat LaporanActivity dan ganti di sini
             startActivity(Intent(this, LaporanActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_menu).setOnClickListener {
             // Ini mengarah ke ManajemenMenuActivity yang sudah ada
            startActivity(Intent(this, ManajemenMenuActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_pengeluaran).setOnClickListener {
            // TODO: Buat PengeluaranActivity dan ganti di sini
             startActivity(Intent(this, PengeluaranActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_riwayat_pesanan).setOnClickListener {
            // TODO: Buat RiwayatPesananAdminActivity dan ganti di sini
//             startActivity(Intent(this, RiwayatPesananAdminActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_user).setOnClickListener {
            // TODO: Buat ManajemenUserActivity dan ganti di sini
             startActivity(Intent(this, ManajemenUserActivity::class.java))
        }
    }
}