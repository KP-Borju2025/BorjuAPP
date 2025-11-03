package com.kp.borju_kp.customer.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kp.borju_kp.customer.fragment.CreateOrderFragment
import com.kp.borju_kp.customer.fragment.HomeFragment
import com.kp.borju_kp.customer.fragment.OrderHistoryFragment
import com.kp.borju_kp.customer.fragment.ProfileFragment

class CustomerViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 4 // Kembali ke 4 item

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> CreateOrderFragment()
            2 -> OrderHistoryFragment()
            3 -> ProfileFragment()
            // Kasus untuk CartFragment dihapus
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
}