package com.kp.borju_kp.customer.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kp.borju_kp.R
import com.kp.borju_kp.customer.OrderDetailActivity
import com.kp.borju_kp.customer.adapter.OrderHistoryAdapter
import com.kp.borju_kp.data.Order
import com.kp.borju_kp.utils.SessionManager

class CompletedOrdersFragment : Fragment() {

    private lateinit var rvCompletedOrders: RecyclerView
    private lateinit var tvNoOrders: TextView
    private lateinit var orderAdapter: OrderHistoryAdapter
    private val orderList = mutableListOf<Order>()

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_completed_orders, container, false)

        rvCompletedOrders = view.findViewById(R.id.rv_completed_orders)
        tvNoOrders = view.findViewById(R.id.tv_no_completed_orders)

        setupRecyclerView()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadCompletedOrders()
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderHistoryAdapter(orderList) { selectedOrder ->
            val intent = Intent(activity, OrderDetailActivity::class.java).apply {
                putExtra("order_detail", selectedOrder)
            }
            startActivity(intent)
        }
        rvCompletedOrders.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = orderAdapter
        }
    }

    private fun loadCompletedOrders() {
        val userId = SessionManager.getUserId()
        if (userId == null) {
            tvNoOrders.visibility = View.VISIBLE
            rvCompletedOrders.visibility = View.GONE
            return
        }

        val completedStatuses = listOf("Selesai", "Dibatalkan")

        db.collection("orders")
            .whereEqualTo("customerId", userId)
            .whereIn("status", completedStatuses)
            .orderBy("orderTimestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    tvNoOrders.visibility = View.VISIBLE
                    rvCompletedOrders.visibility = View.GONE
                } else {
                    tvNoOrders.visibility = View.GONE
                    rvCompletedOrders.visibility = View.VISIBLE
                    orderList.clear()
                    for (doc in documents) {
                        val order = doc.toObject(Order::class.java)
                        order.id = doc.id
                        orderList.add(order)
                    }
                    orderAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("CompletedOrders", "Error getting documents: ", exception)
                Toast.makeText(context, "Gagal memuat riwayat pesanan", Toast.LENGTH_SHORT).show()
            }
    }
}