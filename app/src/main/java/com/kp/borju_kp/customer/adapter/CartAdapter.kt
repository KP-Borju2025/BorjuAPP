package com.kp.borju_kp.customer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kp.borju_kp.R
import com.kp.borju_kp.data.CartItem

class CartAdapter(
    private val cartItems: MutableList<CartItem>,
    private val onIncrease: (CartItem) -> Unit,
    private val onDecrease: (CartItem) -> Unit,
    private val onDelete: (CartItem) -> Unit,
    private val onNoteClick: (CartItem) -> Unit // Listener untuk klik catatan
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]
        holder.bind(item)
        holder.itemView.findViewById<ImageButton>(R.id.btn_increase_quantity).setOnClickListener { onIncrease(item) }
        holder.itemView.findViewById<ImageButton>(R.id.btn_decrease_quantity).setOnClickListener { onDecrease(item) }
        holder.itemView.findViewById<ImageButton>(R.id.btn_delete_item).setOnClickListener { onDelete(item) }
        holder.itemView.findViewById<TextView>(R.id.tv_add_note).setOnClickListener { onNoteClick(item) }
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateData(newCartItems: List<CartItem>) {
        cartItems.clear()
        cartItems.addAll(newCartItems)
        notifyDataSetChanged()
    }

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemName: TextView = itemView.findViewById(R.id.tv_cart_item_name)
        private val itemPrice: TextView = itemView.findViewById(R.id.tv_cart_item_price)
        private val itemQuantity: TextView = itemView.findViewById(R.id.tv_item_quantity)
        private val itemImage: ImageView = itemView.findViewById(R.id.iv_cart_item_image)
        private val tvNote: TextView = itemView.findViewById(R.id.tv_add_note)

        fun bind(cartItem: CartItem) {
            itemName.text = cartItem.menu.name
            itemPrice.text = "Rp ${cartItem.menu.price.toInt()}"
            itemQuantity.text = cartItem.quantity.toString()

            if (cartItem.note.isEmpty()) {
                tvNote.text = "+ Tambah Catatan"
            } else {
                tvNote.text = cartItem.note
            }

            Glide.with(itemView.context)
                .load(cartItem.menu.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(itemImage)
        }
    }
}