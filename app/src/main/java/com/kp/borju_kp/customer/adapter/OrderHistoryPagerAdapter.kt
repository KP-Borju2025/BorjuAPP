package com.kp.borju_kp.customer.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kp.borju_kp.customer.fragment.CompletedOrdersFragment
import com.kp.borju_kp.customer.fragment.OngoingOrdersFragment

class OrderHistoryPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OngoingOrdersFragment()
            1 -> CompletedOrdersFragment()
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
}