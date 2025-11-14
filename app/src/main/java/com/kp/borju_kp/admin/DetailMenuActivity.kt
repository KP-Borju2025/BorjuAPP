package com.kp.borju_kp.admin

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.FirebaseFirestore
import com.kp.borju_kp.R
import com.kp.borju_kp.data.Menu

class DetailMenuActivity : AppCompatActivity() {

    private lateinit var tvMenuName: TextView
    private lateinit var tvMenuPrice: TextView
    private lateinit var tvMenuDescription: TextView
    private lateinit var ivMenuImage: ImageView
    private lateinit var btnAddToCart: Button
    private lateinit var toolbar: MaterialToolbar

    private val db = FirebaseFirestore.getInstance()
    private var currentMenu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_menu)
        enableEdgeToEdge()

        tvMenuName = findViewById(R.id.tv_detail_menu_name)
        tvMenuPrice = findViewById(R.id.tv_detail_menu_price)
        tvMenuDescription = findViewById(R.id.tv_detail_menu_description)
        ivMenuImage = findViewById(R.id.iv_detail_menu_image)
        btnAddToCart = findViewById(R.id.btn_add_to_cart_detail)
        toolbar = findViewById(R.id.toolbar_detail)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val menuId = intent.getStringExtra("MENU_ID")
        if (menuId == null) {
            Toast.makeText(this, "Error: Menu tidak ditemukan", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        fetchMenuDetails(menuId)

        btnAddToCart.setOnClickListener {
            currentMenu?.let {
                CartManager.addItem(it)
                Toast.makeText(this, "${it.name} ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun fetchMenuDetails(menuId: String) {
        db.collection("menus").document(menuId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val menu = document.toObject(Menu::class.java)
                    if (menu != null) {
                        menu.id = document.id
                        currentMenu = menu
                        displayMenuDetails(menu)
                    } else {
                        showErrorAndFinish("Gagal memproses data menu.")
                    }
                } else {
                    showErrorAndFinish("Menu tidak ditemukan di database.")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("DetailMenuActivity", "Error fetching menu details", exception)
                showErrorAndFinish("Gagal mengambil data dari server.")
            }
    }

    private fun displayMenuDetails(menu: Menu) {
        supportActionBar?.title = menu.name
        tvMenuName.text = menu.name
        tvMenuPrice.text = "Rp ${menu.price.toInt()}"
        tvMenuDescription.text = menu.description ?: "Tidak ada deskripsi."
        
        if (menu.imageUrl.isNotEmpty()){
            Glide.with(this).load(menu.imageUrl).centerCrop().into(ivMenuImage)
        }

        // PERBAIKAN UTAMA: Logika untuk menonaktifkan tombol jika stok habis
        if (!menu.status || menu.stok <= 0) {
            btnAddToCart.text = "Stok Habis"
            btnAddToCart.isEnabled = false
            btnAddToCart.isClickable = false
        } else {
            btnAddToCart.text = "Tambah ke Keranjang"
            btnAddToCart.isEnabled = true
            btnAddToCart.isClickable = true
        }
    }

    private fun showErrorAndFinish(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }
}