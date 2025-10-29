package com.kp.borju_kp.customer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kp.borju_kp.R
import com.kp.borju_kp.data.Menu

class MenuAdapter(private val menuList: List<Menu>) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    // Fungsi ini dipanggil saat ViewHolder perlu dibuat
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        // Membuat view dari layout item_menu.xml
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(view)
    }

    // Fungsi ini untuk mengikat data dari list Anda (menuList) ke tampilan di dalam ViewHolder
    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menu = menuList[position]
        holder.foodName.text = menu.name
        holder.foodPrice.text = "Rp ${menu.price}"
        // Anda bisa menggunakan library seperti Glide/Picasso di sini untuk memuat gambar dari menu.imageUrl
        // contoh: Glide.with(holder.itemView.context).load(menu.imageUrl).into(holder.foodImage)
    }

    // Fungsi ini mengembalikan jumlah total item dalam list
    override fun getItemCount(): Int {
        return menuList.size
    }

    // Class ini memegang referensi ke setiap view di dalam item layout (item_menu.xml)
    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodImage: ImageView = itemView.findViewById(R.id.iv_food_image)
        val foodName: TextView = itemView.findViewById(R.id.tv_food_name)
        val foodPrice: TextView = itemView.findViewById(R.id.tv_food_price)
        val addToCartButton: Button = itemView.findViewById(R.id.btn_add_to_cart)
    }
}