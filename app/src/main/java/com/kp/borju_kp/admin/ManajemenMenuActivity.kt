package com.kp.borju_kp.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kp.borju_kp.R
import com.kp.borju_kp.admin.adapter.ManageMenuAdapter
import com.kp.borju_kp.data.Menu

class ManajemenMenuActivity : AppCompatActivity() {

    private lateinit var menuAdapter: ManageMenuAdapter
    private val db = FirebaseFirestore.getInstance()
    private var fullMenuList = listOf<Menu>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manajeman_menu)

        setupRecyclerView()
        setupTabs()

        findViewById<FloatingActionButton>(R.id.fab_tambah_menu).setOnClickListener {
            startActivity(Intent(this, FormTambahMenu::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        fetchMenuData()
    }

    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.rv_menu_admin) // Pastikan ID ini sesuai dengan XML Anda
        recyclerView.layoutManager = GridLayoutManager(this, 1)
        menuAdapter = ManageMenuAdapter(listOf()) { view, menu ->
            showPopupMenu(view, menu)
        }
        recyclerView.adapter = menuAdapter
    }

    private fun setupTabs() {
        val tabLayout: TabLayout = findViewById(R.id.tab_layout_kategori)
        val categories = listOf("Semua", "Makanan", "Minuman", "Snack", "Kopi")
        categories.forEach { category ->
            tabLayout.addTab(tabLayout.newTab().setText(category))
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) { filterByCategory(tab?.text.toString()) }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun fetchMenuData() {
        db.collection("menus")
            .orderBy("name", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                val menuList = result.map { document ->
                    val menu = document.toObject(Menu::class.java)
                    menu.id = document.id
                    menu
                }
                fullMenuList = menuList
                val selectedTab = findViewById<TabLayout>(R.id.tab_layout_kategori).getTabAt(
                    findViewById<TabLayout>(R.id.tab_layout_kategori).selectedTabPosition
                )
                filterByCategory(selectedTab?.text.toString())
            }
            .addOnFailureListener { exception ->
                Log.e("ManajemanMenuActivity", "Error fetching menu", exception)
            }
    }

    private fun filterByCategory(category: String) {
        if (category == "Semua") {
            menuAdapter.updateData(fullMenuList)
        } else {
            val filteredList = fullMenuList.filter { it.kategori.equals(category, ignoreCase = true) }
            menuAdapter.updateData(filteredList)
        }
    }

    private fun showPopupMenu(view: View, menu: Menu) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_manage_options, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    val intent = Intent(this, FormEditMenu::class.java)
                    intent.putExtra("MENU_ID", menu.id)
                    startActivity(intent)
                    true
                }
                R.id.action_delete -> {
                    // Panggil dialog konfirmasi
                    showDeleteConfirmationDialog(menu)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    /**
     * Menampilkan dialog untuk konfirmasi penghapusan.
     */
    private fun showDeleteConfirmationDialog(menu: Menu) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Hapus Menu")
            .setMessage("Anda yakin ingin menghapus menu '${menu.name}'? Tindakan ini tidak dapat dibatalkan.")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Hapus") { _, _ ->
                deleteMenuFromFirestore(menu.id)
            }
            .show()
    }

    /**
     * Menjalankan query hapus ke Firestore berdasarkan ID dokumen.
     */
    private fun deleteMenuFromFirestore(menuId: String) {
        if (menuId.isEmpty()) {
            Toast.makeText(this, "Error: ID Menu tidak valid.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("menus").document(menuId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Menu berhasil dihapus", Toast.LENGTH_SHORT).show()
                fetchMenuData() // Refresh data untuk memperbarui tampilan
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menghapus menu: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("ManajemenMenuActivity", "Error deleting document", e)
            }
    }
}