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
import com.kp.borju_kp.customer.fragment.HomeFragment
import com.kp.borju_kp.data.Menu

class MenuAdapter(
    private val menuList: List<Menu>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    interface OnItemClickListener {
        fun onAddItemClick(menu: Menu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menu = menuList[position]
        holder.foodName.text = menu.name
        holder.foodPrice.text = "Rp ${menu.price.toInt()}"

        if (menu.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(menu.imageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.foodImage)
        }

        holder.addToCartButton.setOnClickListener {
            listener.onAddItemClick(menu)
        }
    }

    override fun getItemCount(): Int {
        return menuList.size
    }

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodImage: ImageView = itemView.findViewById(R.id.iv_food_image)
        val foodName: TextView = itemView.findViewById(R.id.tv_food_name)
        val foodPrice: TextView = itemView.findViewById(R.id.tv_food_price)
        val addToCartButton: Button = itemView.findViewById(R.id.btn_add_to_cart)
    }
}