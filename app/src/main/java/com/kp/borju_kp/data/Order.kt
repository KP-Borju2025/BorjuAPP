package com.kp.borju_kp.data

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Order(
    @get:Exclude var id: String = "",
    var kodePesanan: String = "", // Field baru untuk ID yang ditampilkan
    val customerId: String = "",
    val customerName: String = "",
    val shippingAddress: String = "",
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