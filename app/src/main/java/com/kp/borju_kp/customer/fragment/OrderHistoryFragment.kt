package com.kp.borju_kp.customer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kp.borju_kp.R
import com.kp.borju_kp.customer.adapter.OrderHistoryPagerAdapter

class OrderHistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_order_history, container, false)

        val viewPager: ViewPager2 = view.findViewById(R.id.view_pager_history)
        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)

        // Setup adapter
        val adapter = OrderHistoryPagerAdapter(this)
        viewPager.adapter = adapter

        // Menghubungkan TabLayout dengan ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Berlangsung"
                1 -> "Selesai"
                else -> null
            }
        }.attach()

        return view
    }
}