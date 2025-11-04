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
import com.google.firebase.firestore.Source
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

        return view
    }

    override fun onResume() {
        super.onResume()
        fetchFavoriteMenus()
        fetchAllMenus()
    }

    override fun onAddItemClick(menu: Menu) {
        if (menu.status && menu.stok > 0) {
            orderViewModel.addItem(menu)
            Toast.makeText(context, "${menu.name} ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Maaf, ${menu.name} tidak tersedia saat ini", Toast.LENGTH_SHORT).show()
        }
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

        rvFavoriteMenu.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            private var startX = 0f
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> rv.parent.requestDisallowInterceptTouchEvent(true)
                    MotionEvent.ACTION_MOVE -> {
                        val dx = abs(e.x - startX)
                        if (dx > 5) rv.parent.requestDisallowInterceptTouchEvent(true)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> rv.parent.requestDisallowInterceptTouchEvent(false)
                }
                startX = e.x
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
        db.collection("orders").get(Source.SERVER)
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
                val topMenuNames = menuSalesCount.entries.sortedByDescending { it.value }.map { it.key }.take(5)
                if (topMenuNames.isNotEmpty()) {
                    db.collection("menus").whereIn("name", topMenuNames).get(Source.SERVER)
                        .addOnSuccessListener { menuDocuments ->
                            val tempMenus = mutableListOf<Menu>()
                            for (doc in menuDocuments) {
                                val menu = doc.toObject(Menu::class.java)
                                menu.id = doc.id
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
        db.collection("menus").get(Source.SERVER)
            .addOnSuccessListener { documents ->
                mainMenuList.clear()
                for (document in documents) {
                    // --- KODE DIAGNOSTIK DIMULAI ---
                    val menuName = document.getString("name") ?: "[NAMA TIDAK DITEMUKAN]"
                    val statusValue = document.get("status")
                    Log.d("DIAGNOSTIC", "Menu: '$menuName' | Raw Status Value: '$statusValue' | Type: ${statusValue?.javaClass?.simpleName}")
                    // --- KODE DIAGNOSTIK SELESAI ---

                    val menu = document.toObject(Menu::class.java)
                    menu.id = document.id
                    mainMenuList.add(menu)
                }
                filterMenus(null)
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
             if(isAdded){
                val currentItem = imageSlider.currentItem
                val nextItem = if (currentItem == imageSliderAdapter.itemCount - 1) 0 else currentItem + 1
                imageSlider.setCurrentItem(nextItem, true)
            }
        }
        Timer().schedule(object : TimerTask() {
            override fun run() {
                 if(isAdded) {
                    sliderHandler.post(sliderRunnable)
                 }
            }
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
                position < -1 -> { alpha = 0f }
                position <= 1 -> {
                    val scaleFactor = MIN_SCALE.coerceAtLeast(1 - abs(position))
                    val vertMargin = pageHeight * (1 - scaleFactor) / 2
                    val horzMargin = pageWidth * (1 - scaleFactor) / 2
                    translationX = if (position < 0) {
                        horzMargin - vertMargin / 2
                    } else {
                        -horzMargin + vertMargin / 2
                    }
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                    alpha = (MIN_ALPHA + (((scaleFactor - MIN_SCALE) / (1 - MIN_SCALE)) * (1 - MIN_ALPHA)))
                }
                else -> { alpha = 0f }
            }
        }
    }
}