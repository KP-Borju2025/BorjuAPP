package com.kp.borju_kp.customer.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.kp.borju_kp.R
import com.kp.borju_kp.data.Order
import java.text.SimpleDateFormat
import java.util.Locale

class OrderHistoryAdapter(
    private val orderList: List<Order>,
    private val onItemClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_history, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orderList[position]
        holder.bind(order)
        holder.itemView.setOnClickListener { onItemClick(order) }
    }

    override fun getItemCount(): Int = orderList.size

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderId: TextView = itemView.findViewById(R.id.tv_order_id)
        private val tvOrderedItems: TextView = itemView.findViewById(R.id.tv_ordered_items)
        private val tvOrderDate: TextView = itemView.findViewById(R.id.tv_order_date)
        private val tvOrderTotal: TextView = itemView.findViewById(R.id.tv_order_total)
        private val chipStatus: Chip = itemView.findViewById(R.id.chip_order_status)

        fun bind(order: Order) {
            val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
            val dateString = order.orderTimestamp?.let { sdf.format(it) } ?: "Tanggal tidak tersedia"

            val itemsSummary = order.items.joinToString(separator = ", ") { it.menuName }

            tvOrderId.text = "ID: ${order.id}"
            tvOrderedItems.text = itemsSummary
            tvOrderDate.text = dateString
            tvOrderTotal.text = "Rp ${order.totalPrice.toInt()}"
            chipStatus.text = order.status

            // Mengatur warna Chip berdasarkan status
            when (order.status) {
                "Selesai" -> {
                    chipStatus.setChipBackgroundColorResource(R.color.green_light)
                    chipStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.green_dark))
                }
                "Dibatalkan" -> {
                    chipStatus.setChipBackgroundColorResource(R.color.red_light)
                    chipStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.red_dark))
                }
                else -> { // Menunggu Konfirmasi, Diproses, dll.
                    chipStatus.setChipBackgroundColorResource(R.color.orange_light)
                    chipStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.orange_dark))
                }
            }
        }
    }
}