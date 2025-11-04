package com.kp.borju_kp.customer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kp.borju_kp.R
import com.kp.borju_kp.data.Menu

class MenuAdapter(
    private val menuList: List<Menu>,
    private val listener: OnMenuClickListener
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    interface OnMenuClickListener {
        fun onAddItemClick(menu: Menu)
        fun onItemClick(menu: Menu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menu = menuList[position]
        holder.bind(menu)
    }

    override fun getItemCount(): Int {
        return menuList.size
    }

    inner class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val foodImage: ImageView = itemView.findViewById(R.id.iv_food_image)
        private val foodName: TextView = itemView.findViewById(R.id.tv_food_name)
        private val foodPrice: TextView = itemView.findViewById(R.id.tv_food_price)
        private val addToCartButton: Button = itemView.findViewById(R.id.btn_add_to_cart)

        fun bind(menu: Menu) {
            foodName.text = menu.name
            foodPrice.text = "Rp ${menu.price.toInt()}"

            if (menu.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(menu.imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(foodImage)
            }

            if (menu.status && menu.stok > 0) {
                addToCartButton.text = "Tambah"
                addToCartButton.isEnabled = true
                addToCartButton.setOnClickListener { listener.onAddItemClick(menu) }
            } else {
                addToCartButton.text = if (!menu.status) "Tidak Tersedia" else "Stok Habis"
                addToCartButton.isEnabled = false
            }

            itemView.setOnClickListener {
                listener.onItemClick(menu)
            }
        }
    }
}