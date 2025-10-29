package com.kp.borju_kp.customer

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kp.borju_kp.R
import com.kp.borju_kp.customer.adapter.CustomerViewPagerAdapter

class DashboardCostumer : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_costumer)
        enableEdgeToEdge()

        val viewPager: ViewPager2 = findViewById(R.id.view_pager)
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Set up ViewPager Adapter
        val adapter = CustomerViewPagerAdapter(this)
        viewPager.adapter = adapter

        // --- Sync ViewPager2 with BottomNavigationView ---

        // 1. When swiping the ViewPager, update the BottomNav
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bottomNav.menu.getItem(position).isChecked = true
            }
        })

        // 2. When tapping an item on the BottomNav, update the ViewPager
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    viewPager.currentItem = 0
                    true
                }
                R.id.nav_order -> {
                    viewPager.currentItem = 1
                    true
                }
                R.id.nav_history -> {
                    viewPager.currentItem = 2
                    true
                }
                R.id.nav_profile -> {
                    viewPager.currentItem = 3
                    true
                }
                else -> false
            }
        }
    }
}