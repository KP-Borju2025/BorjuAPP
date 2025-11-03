package com.kp.borju_kp.customer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kp.borju_kp.R
import com.kp.borju_kp.data.Menu

class FavoriteMenuAdapter(private var menuList: List<Menu>) : RecyclerView.Adapter<FavoriteMenuAdapter.FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite_menu, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(menuList[position])
    }

    override fun getItemCount(): Int = menuList.size

    fun updateData(newMenuList: List<Menu>) {
        menuList = newMenuList
        notifyDataSetChanged()
    }

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.tv_food_name)
        private val price: TextView = itemView.findViewById(R.id.tv_food_price)
        private val image: ImageView = itemView.findViewById(R.id.iv_food_image)

        fun bind(menu: Menu) {
            name.text = menu.name
            price.text = "Rp ${menu.price.toInt()}"

            if (menu.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(menu.imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_background) // Placeholder image
                    .into(image)
            }
        }
    }
}