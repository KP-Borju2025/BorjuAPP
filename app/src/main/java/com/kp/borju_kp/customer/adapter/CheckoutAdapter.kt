package com.kp.borju_kp.customer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kp.borju_kp.R
import com.kp.borju_kp.data.CartItem

class CheckoutAdapter(private val cartItems: List<CartItem>) : RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_checkout, parent, false)
        return CheckoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: CheckoutViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount(): Int = cartItems.size

    class CheckoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemName: TextView = itemView.findViewById(R.id.tv_checkout_item_name)
        private val itemQuantity: TextView = itemView.findViewById(R.id.tv_checkout_item_quantity)
        private val itemTotalPrice: TextView = itemView.findViewById(R.id.tv_checkout_item_total_price)
        private val itemNote: TextView = itemView.findViewById(R.id.tv_checkout_item_note) // Referensi ke TextView catatan

        fun bind(cartItem: CartItem) {
            itemName.text = cartItem.menu.name
            itemQuantity.text = "${cartItem.quantity}x"
            val totalPrice = cartItem.menu.price * cartItem.quantity
            itemTotalPrice.text = "Rp ${totalPrice.toInt()}"

            // Logika untuk menampilkan atau menyembunyikan catatan
            if (cartItem.note.isNotEmpty()) {
                itemNote.visibility = View.VISIBLE
                itemNote.text = "Catatan: ${cartItem.note}"
            } else {
                itemNote.visibility = View.GONE
            }
        }
    }
}