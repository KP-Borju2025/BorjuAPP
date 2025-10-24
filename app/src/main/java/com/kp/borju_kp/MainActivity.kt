package com.kp.borju_kp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT: Long = 3000 // 3 detik

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Handler untuk menunda perpindahan Activity
        Handler().postDelayed({
            // Jalankan Activity Login setelah timer habis
            val intent = Intent(this, LoginActivity::class.java) // Ganti MainActivity dengan Activity Login kamu
            startActivity(intent)

            // Tutup Splash Activity agar tidak bisa kembali dengan tombol Back
            finish()
        }, SPLASH_TIME_OUT)
    }
}