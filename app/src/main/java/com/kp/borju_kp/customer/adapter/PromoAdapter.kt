package com.kp.borju_kp.customer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kp.borju_kp.R
import com.kp.borju_kp.data.Promo

class PromoAdapter(private val promoList: List<Promo>) : RecyclerView.Adapter<PromoAdapter.PromoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_promo, parent, false)
        return PromoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PromoViewHolder, position: Int) {
        val promo = promoList[position]
        holder.promoTitle.text = promo.title
        holder.promoDescription.text = promo.description
        // You can use Glide or Picasso here to load the image from promo.imageUrl
    }

    override fun getItemCount() = promoList.size

    class PromoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val promoImage: ImageView = itemView.findViewById(R.id.iv_promo_image)
        val promoTitle: TextView = itemView.findViewById(R.id.tv_promo_title)
        val promoDescription: TextView = itemView.findViewById(R.id.tv_promo_description)
    }
}