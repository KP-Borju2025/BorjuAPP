package com.kp.borju_kp.admin.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kp.borju_kp.R
import com.kp.borju_kp.admin.CartItem
import com.kp.borju_kp.admin.CartManager

class CheckoutAdapter(
    private var cartItems: List<CartItem>,
    private val listener: OnCartUpdateListener
) : RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder>() {

    interface OnCartUpdateListener {
        fun onCartUpdated()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutViewHolder {
        // PERBAIKAN: Menggunakan layout item_checkout_admin
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_checkout_admin, parent, false)
        return CheckoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: CheckoutViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount(): Int = cartItems.size

    inner class CheckoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemImage: ImageView = itemView.findViewById(R.id.iv_checkout_item_image)
        private val itemName: TextView = itemView.findViewById(R.id.tv_item_name)
        private val itemPrice: TextView = itemView.findViewById(R.id.tv_item_price)
        private val itemQuantity: TextView = itemView.findViewById(R.id.tv_item_quantity)
        private val itemNote: EditText = itemView.findViewById(R.id.et_item_note)
        private val btnIncrease: Button = itemView.findViewById(R.id.btn_increase_quantity)
        private val btnDecrease: Button = itemView.findViewById(R.id.btn_decrease_quantity)
        private val btnRemove: ImageButton = itemView.findViewById(R.id.btn_remove_item)

        fun bind(cartItem: CartItem) {
            itemName.text = cartItem.menu.name
            itemPrice.text = "@ Rp ${cartItem.menu.price.toInt()}"
            itemQuantity.text = cartItem.quantity.toString()

            Glide.with(itemView.context).load(cartItem.menu.imageUrl).centerCrop().into(itemImage)

            itemNote.removeTextChangedListener(itemNote.tag as? TextWatcher)
            itemNote.setText(cartItem.note)
            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) { cartItem.note = s.toString() }
            }
            itemNote.addTextChangedListener(textWatcher)
            itemNote.tag = textWatcher

            btnIncrease.setOnClickListener {
                CartManager.increaseItemQuantity(cartItem)
                listener.onCartUpdated() 
            }

            btnDecrease.setOnClickListener {
                CartManager.decreaseItemQuantity(cartItem)
                listener.onCartUpdated()
            }

            btnRemove.setOnClickListener {
                CartManager.removeItem(cartItem)
                listener.onCartUpdated()
            }
        }
    }
}