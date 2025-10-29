package com.kp.borju_kp.data

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Pengeluaran(
    @get:Exclude var id: String = "",
    val namaPengeluaran: String = "",
    val jumlah: Double = 0.0,
    val kategori: String = "", // Ditambahkan
    @ServerTimestamp
    val tanggal: Date? = null
)