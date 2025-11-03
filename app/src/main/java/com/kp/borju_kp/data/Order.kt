package com.kp.borju_kp.data

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Order(
    @get:Exclude var id: String = "",
    val customerName: String = "",
    val shippingAddress: String = "", // Properti untuk alamat pengiriman
    val paymentMethod: String = "",
    val totalPrice: Double = 0.0,
    val status: String = "Baru",
    val orderType: String = "Offline",
    val paymentProofUrl: String = "",
    @ServerTimestamp
    val orderTimestamp: Date? = null,
    val items: List<OrderItem> = listOf()
) : Parcelable

@Parcelize
data class OrderItem(
    val menuId: String = "",
    val menuName: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val note: String = ""
) : Parcelable