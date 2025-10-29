package com.kp.borju_kp.customer.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.kp.borju_kp.R
import com.kp.borju_kp.customer.MenuAdapter
import com.kp.borju_kp.data.Menu

class CreateOrderFragment : Fragment() {

    private lateinit var menuAdapter: MenuAdapter
    private val menuList = ArrayList<Menu>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_order, container, false)

        // Setup RecyclerView dengan list kosong pada awalnya
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_menu_order)
        recyclerView.layoutManager = LinearLayoutManager(context)
        menuAdapter = MenuAdapter(menuList)
        recyclerView.adapter = menuAdapter

        // Ambil data dari Firestore
        fetchMenuData()

        val fabCart = view.findViewById<FloatingActionButton>(R.id.fab_cart)
        fabCart.setOnClickListener {
            Toast.makeText(context, "Buka Keranjang", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun fetchMenuData() {
        // Ganti "menus" dengan nama collection Anda di Firestore
        db.collection("menus")
            .get()
            .addOnSuccessListener { result ->
                // Hapus data lama sebelum menambahkan data baru
                menuList.clear()
                for (document in result) {
                    // Secara otomatis Firestore akan mengubah dokumen menjadi data class Menu
                    // Pastikan nama field di Firestore (misal: "name", "price", "imageUrl")
                    // sama persis dengan nama properti di data class Menu Anda.
                    val menu = document.toObject(Menu::class.java)
                    menuList.add(menu)
                }
                // Beri tahu adapter bahwa datanya sudah berubah dan RecyclerView harus di-update
                menuAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("CreateOrderFragment", "Error getting documents.", exception)
                Toast.makeText(context, "Gagal memuat menu", Toast.LENGTH_SHORT).show()
            }
    }
}