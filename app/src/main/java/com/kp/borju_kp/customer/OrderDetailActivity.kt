package com.kp.borju_kp.customer

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.kp.borju_kp.R
import com.kp.borju_kp.data.Order
import com.kp.borju_kp.data.OrderItem
import java.text.SimpleDateFormat
import java.util.Locale

class OrderDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val order = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("order_detail", Order::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("order_detail")
        }

        if (order != null) {
            displayOrderDetails(order)
        }
    }

    private fun displayOrderDetails(order: Order) {
        findViewById<TextView>(R.id.tv_detail_order_id).text = "ID: ${order.id}"
        findViewById<TextView>(R.id.tv_detail_payment_method).text = order.paymentMethod
        findViewById<TextView>(R.id.tv_detail_shipping_address).text = order.shippingAddress
        findViewById<TextView>(R.id.tv_detail_total_price).text = "Total: Rp ${order.totalPrice.toInt()}"

        val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
        val dateString = order.orderTimestamp?.let { sdf.format(it) } ?: "N/A"
        findViewById<TextView>(R.id.tv_detail_order_date).text = dateString
        
        val chipStatus: Chip = findViewById(R.id.chip_detail_order_status)
        chipStatus.text = order.status
        when (order.status) {
            "Selesai" -> {
                chipStatus.setChipBackgroundColorResource(R.color.green_light)
                chipStatus.setTextColor(ContextCompat.getColor(this, R.color.green_dark))
            }
            "Dibatalkan" -> {
                chipStatus.setChipBackgroundColorResource(R.color.red_light)
                chipStatus.setTextColor(ContextCompat.getColor(this, R.color.red_dark))
            }
            else -> {
                chipStatus.setChipBackgroundColorResource(R.color.orange_light)
                chipStatus.setTextColor(ContextCompat.getColor(this, R.color.orange_dark))
            }
        }

        val rvItems: RecyclerView = findViewById(R.id.rv_order_detail_items)
        rvItems.layoutManager = LinearLayoutManager(this)
        rvItems.adapter = OrderDetailItemAdapter(order.items)
    }
}

class OrderDetailItemAdapter(private val items: List<OrderItem>) : RecyclerView.Adapter<OrderDetailItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: OrderItem) {
            val itemInfo: TextView = itemView.findViewById(R.id.tv_detail_item_info)
            val itemNote: TextView = itemView.findViewById(R.id.tv_detail_item_note)
            itemInfo.text = "${item.quantity}x ${item.menuName}"
            if (item.note.isNotEmpty()) {
                itemNote.visibility = View.VISIBLE
                itemNote.text = "Catatan: ${item.note}"
            } else {
                itemNote.visibility = View.GONE
            }
        }
    }
}