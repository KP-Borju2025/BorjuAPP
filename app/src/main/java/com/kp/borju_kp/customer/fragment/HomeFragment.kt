package com.kp.borju_kp.customer.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
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
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.kp.borju_kp.R
import com.kp.borju_kp.customer.adapter.FavoriteMenuAdapter
import com.kp.borju_kp.customer.adapter.ImageSliderAdapter
import com.kp.borju_kp.customer.adapter.MenuAdapter
import com.kp.borju_kp.data.Menu
import com.kp.borju_kp.viewmodel.OrderViewModel
import java.util.Timer
import java.util.TimerTask
import kotlin.math.abs

class HomeFragment : Fragment(), MenuAdapter.OnMenuClickListener, FavoriteMenuAdapter.OnItemClickListener {

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

    private lateinit var mainMenuAdapter: MenuAdapter
    private val mainMenuList = ArrayList<Menu>()
    private val filteredMenuList = ArrayList<Menu>()

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
        setupFilter(view)

        fetchFavoriteMenus()
        fetchAllMenus()

        return view
    }

    override fun onAddItemClick(menu: Menu) {
        orderViewModel.addItem(menu)
        Toast.makeText(context, "${menu.name} ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
    }

    override fun onItemClick(menu: Menu) {
        MenuDetailBottomSheetFragment.newInstance(menu).show(parentFragmentManager, MenuDetailBottomSheetFragment.TAG)
    }

    private fun setupFavoriteMenu(view: View) {
        rvFavoriteMenu = view.findViewById(R.id.rv_favorite_menu)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvFavoriteMenu.layoutManager = layoutManager
        favoriteMenuAdapter = FavoriteMenuAdapter(favoriteMenuList, this)
        rvFavoriteMenu.adapter = favoriteMenuAdapter

        // Menambahkan listener untuk mengatasi konflik scroll
        rvFavoriteMenu.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            private var startX = 0f
            private var startY = 0f

            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = e.x
                        startY = e.y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = abs(e.x - startX)
                        val dy = abs(e.y - startY)
                        if (dx > dy) {
                            rv.parent.requestDisallowInterceptTouchEvent(true)
                        }
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }

    private fun setupMainMenu(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_home)
        recyclerView.layoutManager = LinearLayoutManager(context)
        mainMenuAdapter = MenuAdapter(filteredMenuList, this)
        recyclerView.adapter = mainMenuAdapter
    }

    private fun setupFilter(view: View) {
        val chipGroup = view.findViewById<ChipGroup>(R.id.chip_group_filter)
        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedCategory = when (checkedId) {
                R.id.chip_food -> "Makanan"
                R.id.chip_drink -> "Minuman"
                R.id.chip_snack -> "Snack"
                R.id.chip_coffee -> "Kopi"
                else -> null
            }
            filterMenus(selectedCategory)
        }
    }

    private fun filterMenus(category: String?) {
        filteredMenuList.clear()
        if (category == null) {
            filteredMenuList.addAll(mainMenuList)
        } else {
            filteredMenuList.addAll(mainMenuList.filter { it.kategori == category })
        }
        mainMenuAdapter.notifyDataSetChanged()
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
                filterMenus(null) // Tampilkan semua menu pada awalnya
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

        imageSlider.setPageTransformer(ZoomOutPageTransformer())

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
    }
}

private const val MIN_SCALE = 0.85f
private const val MIN_ALPHA = 0.5f

class ZoomOutPageTransformer : ViewPager2.PageTransformer {

    override fun transformPage(view: View, position: Float) {
        view.apply {
            val pageWidth = width
            val pageHeight = height
            when {
                position < -1 -> { // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    alpha = 0f
                }
                position <= 1 -> { // [-1,1]
                    // Modify the default slide transition to shrink the page as well
                    val scaleFactor = MIN_SCALE.coerceAtLeast(1 - abs(position))
                    val vertMargin = pageHeight * (1 - scaleFactor) / 2
                    val horzMargin = pageWidth * (1 - scaleFactor) / 2
                    translationX = if (position < 0) {
                        horzMargin - vertMargin / 2
                    } else {
                        -horzMargin + vertMargin / 2
                    }

                    // Scale the page down (between MIN_SCALE and 1)
                    scaleX = scaleFactor
                    scaleY = scaleFactor

                    // Fade the page relative to its size.
                    alpha = (MIN_ALPHA +
                            (((scaleFactor - MIN_SCALE) / (1 - MIN_SCALE)) * (1 - MIN_ALPHA)))
                }
                else -> { // (1,+Infinity]
                    // This page is way off-screen to the right.
                    alpha = 0f
                }
            }
        }
    }
}