package com.kp.borju_kp.customer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kp.borju_kp.R
import com.kp.borju_kp.customer.adapter.PromoAdapter
import com.kp.borju_kp.data.Promo

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_home)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val promoList = ArrayList<Promo>()
        promoList.add(Promo("Diskon 50%", "Dapatkan diskon 50% untuk semua jenis kopi.", ""))
        promoList.add(Promo("Beli 1 Gratis 1", "Beli 1 makanan, gratis 1 minuman.", ""))

        val adapter = PromoAdapter(promoList)
        recyclerView.adapter = adapter

        return view
    }
}