package com.kp.borju_kp.admin.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kp.borju_kp.R
import com.kp.borju_kp.data.Menu

class ManageMenuAdapter(
    private var menuList: List<Menu>,
    private val onOptionsMenuClicked: (View, Menu) -> Unit
) : RecyclerView.Adapter<ManageMenuAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_manage_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menu = menuList[position]
        holder.bind(menu)
    }

    override fun getItemCount(): Int = menuList.size

    fun updateData(newMenuList: List<Menu>) {
        menuList = newMenuList
        notifyDataSetChanged()
    }

    inner class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.tv_food_name)
        private val price: TextView = itemView.findViewById(R.id.tv_food_price)
        private val image: ImageView = itemView.findViewById(R.id.iv_food_image)
        private val stockStatus: TextView = itemView.findViewById(R.id.tv_stock_status)
        private val options: ImageButton = itemView.findViewById(R.id.btn_options_menu)

        fun bind(menu: Menu) {
            name.text = menu.name
            price.text = "Rp ${menu.price.toInt()}"

            // Muat gambar dengan Glide
            if (menu.imageUrl.isNotEmpty()) {
                 Glide.with(itemView.context)
                    .load(menu.imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_background) // Ganti dengan placeholder Anda
                    .into(image)
            }

            // Atur status stok dan warna teks
            if (!menu.status || menu.stok <= 0) {
                stockStatus.text = "Stok: Habis"
                stockStatus.setTextColor(Color.RED)
            } else {
                stockStatus.text = "Stok: ${menu.stok}"
                stockStatus.setTextColor(Color.DKGRAY)
            }

            // Set listener untuk tombol opsi
            options.setOnClickListener { onOptionsMenuClicked(it, menu) }
        }
    }
}