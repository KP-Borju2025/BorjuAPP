package com.kp.borju_kp

import android.app.Application
import com.cloudinary.android.MediaManager
import com.kp.borju_kp.utils.SessionManager

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inisialisasi SessionManager
        SessionManager.init(this)

        // Inisialisasi Cloudinary
        val config = HashMap<String, String>()
        config["cloud_name"] = "NAMA_CLOUD_ANDA" // Ganti dengan nama cloud Anda
        config["api_key"] = "317242821532517"       // Ganti dengan API key Anda
        config["api_secret"] = "API_SECRET_ANDA" // Ganti dengan API secret Anda
        MediaManager.init(this, config)
    }
}