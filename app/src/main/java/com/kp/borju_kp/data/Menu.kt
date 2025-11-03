package com.kp.borju_kp.data

import com.google.firebase.firestore.Exclude

data class Menu(
    @get:Exclude var id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val priceBuy : Double = 0.0,
    val imageUrl: String = "",
    val kategori: String = "",
    val description: String = "",
    val stok: Int = 0, // <-- DITAMBAHKAN: Properti untuk jumlah stok
    val status: Boolean = true // true = Tersedia, false = Tidak Tersedia
)
