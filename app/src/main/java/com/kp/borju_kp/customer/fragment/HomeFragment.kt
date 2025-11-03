package com.kp.borju_kp.customer.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.firestore.FirebaseFirestore
import com.kp.borju_kp.R
import com.kp.borju_kp.customer.adapter.FavoriteMenuAdapter
import com.kp.borju_kp.customer.adapter.ImageSliderAdapter
import com.kp.borju_kp.customer.adapter.MenuAdapter
import com.kp.borju_kp.data.Menu
import com.kp.borju_kp.viewmodel.OrderViewModel
import java.util.Timer
import java.util.TimerTask

class HomeFragment : Fragment(), MenuAdapter.OnItemClickListener {

    private val orderViewModel: OrderViewModel by activityViewModels()

    private lateinit var imageSlider: ViewPager2
    private lateinit var indicatorContainer: LinearLayout
    private lateinit var imageSliderAdapter: ImageSliderAdapter
    private val imageList = ArrayList<Int>()
    private lateinit var sliderHandler: Handler
    private lateinit var sliderRunnable: Runnable

    private lateinit var rvFavoriteMenu: RecyclerView
    private lateinit var favoriteMenuAdapter: FavoriteMenuAdapter
    private val favoriteMenuList = ArrayList<Menu>()
    private var favoriteScrollTimer: Timer? = null

    private lateinit var mainMenuAdapter: MenuAdapter
    private val mainMenuList = ArrayList<Menu>()

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        setupImageSlider(view)
        setupFavoriteMenu(view)
        setupMainMenu(view)

        fetchFavoriteMenus()
        fetchAllMenus()

        return view
    }

    override fun onAddItemClick(menu: Menu) {
        orderViewModel.addItem(menu)
        Toast.makeText(context, "${menu.name} ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
    }

    private fun setupFavoriteMenu(view: View) {
        rvFavoriteMenu = view.findViewById(R.id.rv_favorite_menu)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvFavoriteMenu.layoutManager = layoutManager
        favoriteMenuAdapter = FavoriteMenuAdapter(favoriteMenuList)
        rvFavoriteMenu.adapter = favoriteMenuAdapter

        startFavoriteMenuAutoScroll()
    }

    private fun startFavoriteMenuAutoScroll() {
        favoriteScrollTimer?.cancel()
        favoriteScrollTimer = Timer()

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            var currentPosition = 0
            var forward = true

            override fun run() {
                if (favoriteMenuAdapter.itemCount > 0) {
                    if (forward) {
                        currentPosition++
                        if (currentPosition >= favoriteMenuAdapter.itemCount) {
                            currentPosition--
                            forward = false
                        }
                    } else {
                        currentPosition--
                        if (currentPosition < 0) {
                            currentPosition++
                            forward = true
                        }
                    }
                    rvFavoriteMenu.smoothScrollToPosition(currentPosition)
                }
            }
        }

        favoriteScrollTimer?.schedule(object : TimerTask() {
            override fun run() {
                handler.post(runnable)
            }
        }, 5000, 5000)
    }

    private fun setupMainMenu(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_home)
        recyclerView.layoutManager = LinearLayoutManager(context)
        mainMenuAdapter = MenuAdapter(mainMenuList, this)
        recyclerView.adapter = mainMenuAdapter
    }

    private fun fetchFavoriteMenus() {
        db.collection("orders").get()
            .addOnSuccessListener { salesDocuments ->
                val menuSalesCount = mutableMapOf<String, Int>()
                for (document in salesDocuments) {
                    val items = document.get("items") as? List<Map<String, Any>>
                    items?.forEach { item ->
                        val menuName = item["menuName"] as? String
                        val quantity = (item["quantity"] as? Long)?.toInt() ?: 0
                        if (menuName != null) {
                            menuSalesCount[menuName] = (menuSalesCount[menuName] ?: 0) + quantity
                        }
                    }
                }
                val topMenuNames = menuSalesCount.entries.sortedByDescending { it.value }.map { it.key }
                if (topMenuNames.isNotEmpty()) {
                    db.collection("menus").whereIn("name", topMenuNames).get()
                        .addOnSuccessListener { menuDocuments ->
                            val tempMenus = mutableListOf<Menu>()
                            for (doc in menuDocuments) {
                                val menu = doc.toObject(Menu::class.java)
                                menu.id = doc.id // Menyimpan ID Dokumen
                                tempMenus.add(menu)
                            }
                            val sortedFavoriteMenus = topMenuNames.mapNotNull { name -> tempMenus.find { it.name == name } }
                            favoriteMenuList.clear()
                            favoriteMenuList.addAll(sortedFavoriteMenus)
                            favoriteMenuAdapter.updateData(favoriteMenuList)
                        }.addOnFailureListener { e -> Log.w("HomeFragment", "Error getting menu details", e) }
                }
            }.addOnFailureListener { e -> Log.w("HomeFragment", "Error getting sales data", e) }
    }

    private fun fetchAllMenus() {
        db.collection("menus").get()
            .addOnSuccessListener { documents ->
                mainMenuList.clear()
                for (document in documents) {
                    val menu = document.toObject(Menu::class.java)
                    menu.id = document.id // PERBAIKAN: Menyimpan ID Dokumen
                    mainMenuList.add(menu)
                }
                mainMenuAdapter.notifyDataSetChanged()
            }.addOnFailureListener { e -> Log.w("HomeFragment", "Error getting all menus", e) }
    }

    private fun setupImageSlider(view: View) {
        imageSlider = view.findViewById(R.id.image_slider)
        indicatorContainer = view.findViewById(R.id.indicator_container)
        imageList.add(R.drawable.ic_launcher_background)
        imageList.add(R.drawable.ic_launcher_foreground)
        imageList.add(R.drawable.ic_launcher_background)
        imageSliderAdapter = ImageSliderAdapter(imageList)
        imageSlider.adapter = imageSliderAdapter
        setupIndicators()
        setCurrentIndicator(0)
        imageSlider.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
            }
        })
        sliderHandler = Handler(Looper.getMainLooper())
        sliderRunnable = Runnable {
            val currentItem = imageSlider.currentItem
            val nextItem = if (currentItem == imageSliderAdapter.itemCount - 1) 0 else currentItem + 1
            imageSlider.setCurrentItem(nextItem, true)
        }
        Timer().schedule(object : TimerTask() {
            override fun run() { sliderHandler.post(sliderRunnable) }
        }, 3000, 3000)
    }

    private fun setupIndicators() {
        if (!isAdded) return
        val indicators = arrayOfNulls<ImageView>(imageSliderAdapter.itemCount)
        val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(8, 0, 8, 0)
        indicatorContainer.removeAllViews()
        for (i in indicators.indices) {
            indicators[i] = ImageView(requireContext())
            indicators[i]?.apply {
                setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.indicator_inactive))
                this.layoutParams = layoutParams
            }
            indicatorContainer.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(index: Int) {
        if (!isAdded) return
        val childCount = indicatorContainer.childCount
        for (i in 0 until childCount) {
            val imageView = indicatorContainer.getChildAt(i) as ImageView
            if (i == index) {
                imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.indicator_active))
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.indicator_inactive))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sliderHandler.removeCallbacks(sliderRunnable)
        favoriteScrollTimer?.cancel()
    }
}