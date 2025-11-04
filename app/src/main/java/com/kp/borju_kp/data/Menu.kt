package com.kp.borju_kp.data

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class Menu(
    @get:Exclude var id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val priceBuy : Double = 0.0,
    val imageUrl: String = "",
    val kategori: String = "",
    val description: String = "",
    val stok: Int = 0,
    val status: Boolean = true
) : Parcelable