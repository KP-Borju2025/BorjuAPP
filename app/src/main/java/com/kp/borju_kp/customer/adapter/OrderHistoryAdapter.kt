package com.kp.borju_kp.customer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.kp.borju_kp.R
import com.kp.borju_kp.data.Order
import java.text.SimpleDateFormat
import java.util.Locale

class OrderHistoryAdapter(
    private val onOrderClick: (Order) -> Unit
) : ListAdapter<Order, OrderHistoryAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_history, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orderId: TextView = itemView.findViewById(R.id.tv_order_id)
        private val orderDate: TextView = itemView.findViewById(R.id.tv_order_date)
        private val itemCount: TextView = itemView.findViewById(R.id.tv_item_count)
        private val orderTotal: TextView = itemView.findViewById(R.id.tv_order_total)
        private val orderStatus: Chip = itemView.findViewById(R.id.chip_order_status)

        fun bind(order: Order) {
            // Menggunakan field kodePesanan yang baru
            orderId.text = order.kodePesanan
            orderDate.text = order.orderTimestamp?.let {
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it)
            } ?: "N/A"

            val totalItems = order.items.sumOf { it.quantity }
            itemCount.text = "$totalItems items"

            orderTotal.text = "Rp ${order.totalPrice.toInt()}"
            orderStatus.text = order.status

            orderStatus.setChipBackgroundColorResource(
                when (order.status) {
                    "Selesai" -> R.color.Tertiary
                    "Dibatalkan", "Ditolak" -> R.color.Error
                    "Menunggu Konfirmasi" -> R.color.Secondary
                    else -> R.color.Primary
                }
            )

            itemView.setOnClickListener {
                onOrderClick(getItem(bindingAdapterPosition))
            }
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}
