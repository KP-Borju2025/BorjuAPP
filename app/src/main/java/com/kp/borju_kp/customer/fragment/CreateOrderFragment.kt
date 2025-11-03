package com.kp.borju_kp.customer.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.kp.borju_kp.R
import com.kp.borju_kp.customer.adapter.MenuAdapter
import com.kp.borju_kp.data.Menu
import com.kp.borju_kp.viewmodel.OrderViewModel

class CreateOrderFragment : Fragment(), MenuAdapter.OnMenuClickListener {

    private val orderViewModel: OrderViewModel by activityViewModels()

    private lateinit var menuAdapter: MenuAdapter
    private val menuList = ArrayList<Menu>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_order, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_menu_order)
        recyclerView.layoutManager = LinearLayoutManager(context)
        menuAdapter = MenuAdapter(menuList, this)
        recyclerView.adapter = menuAdapter

        fetchMenuData()

        val fabCart = view.findViewById<FloatingActionButton>(R.id.fab_cart)
        fabCart.setOnClickListener {
            CartBottomSheetFragment().show(parentFragmentManager, CartBottomSheetFragment.TAG)
        }

        return view
    }

    private fun fetchMenuData() {
        db.collection("menus")
            .get()
            .addOnSuccessListener { result ->
                menuList.clear()
                for (document in result) {
                    val menu = document.toObject(Menu::class.java)
                    menu.id = document.id // PERBAIKAN: Menyimpan ID Dokumen
                    menuList.add(menu)
                }
                menuAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("CreateOrderFragment", "Error getting documents.", exception)
                Toast.makeText(context, "Gagal memuat menu", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onAddItemClick(menu: Menu) {
        orderViewModel.addItem(menu)
        Toast.makeText(context, "${menu.name} ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
    }

    override fun onItemClick(menu: Menu) {
        MenuDetailBottomSheetFragment.newInstance(menu).show(parentFragmentManager, MenuDetailBottomSheetFragment.TAG)
    }
}