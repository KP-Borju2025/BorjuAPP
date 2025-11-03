package com.kp.borju_kp.customer

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kp.borju_kp.R
import com.kp.borju_kp.customer.adapter.CustomerViewPagerAdapter

class DashboardCostumer : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_costumer)
        enableEdgeToEdge()

        viewPager = findViewById(R.id.view_pager)
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)

        val adapter = CustomerViewPagerAdapter(this)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bottomNav.menu.getItem(position).isChecked = true
            }
        })

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> viewPager.currentItem = 0
                R.id.nav_order -> viewPager.currentItem = 1
                R.id.nav_history -> viewPager.currentItem = 2
                R.id.nav_profile -> viewPager.currentItem = 3
            }
            true
        }
    }

    fun switchToTab(index: Int) {
        viewPager.currentItem = index
    }
}