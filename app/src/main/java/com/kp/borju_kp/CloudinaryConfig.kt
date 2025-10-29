package com.kp.borju_kp

import com.cloudinary.Cloudinary
import java.util.HashMap

/**
 * Singleton object untuk mengelola satu instance dari Cloudinary di seluruh aplikasi.
 * Menggunakan 'by lazy' memastikan instance hanya dibuat saat pertama kali dibutuhkan.
 */
object CloudinaryConfig {

    val instance: Cloudinary by lazy {
        val config = HashMap<String, String>()

        // --- GANTI DENGAN KREDENSIAL ASLI ANDA DI BAWAH INI ---
        config["cloud_name"] = "dov8bkefl"
        config["api_key"] = "317242821532517"
        config["api_secret"] = "--f6IH0vpamFoCeVK76K4l3idlA"
        // ----------------------------------------------------

        Cloudinary(config)
    }
}