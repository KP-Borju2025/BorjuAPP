package com.kp.borju_kp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(
    val menu: Menu,
    var quantity: Int = 1,
    var note: String = "" // Properti untuk catatan
) : Parcelable