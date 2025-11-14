package com.kp.borju_kp.admin

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kp.borju_kp.CloudinaryConfig
import com.kp.borju_kp.R
import com.kp.borju_kp.data.Menu
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FormEditMenu : AppCompatActivity() {

    private lateinit var imagePreview: ShapeableImageView
    private lateinit var etNamaMenu: TextInputEditText
    private lateinit var etDetailMenu: TextInputEditText
    private lateinit var actvKategori: AutoCompleteTextView
    private lateinit var etHargaJual: TextInputEditText
    private lateinit var etHargaBeli: TextInputEditText
    private lateinit var etStokMenu: TextInputEditText
    private lateinit var switchStatus: SwitchMaterial
    private lateinit var btnSimpan: Button

    private val db = FirebaseFirestore.getInstance()
    private val cloudinary by lazy { CloudinaryConfig.instance }
    private var imageUri: Uri? = null
    private var currentImageUrl: String? = null
    private var menuId: String? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            Glide.with(this).load(it).into(imagePreview)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_edit_menu)
        enableEdgeToEdge()


        menuId = intent.getStringExtra("MENU_ID")
        if (menuId == null) {
            Toast.makeText(this, "ID Menu tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        initViews()
        setupCategoryDropdown()
        loadMenuData()

        findViewById<Button>(R.id.btn_pilih_gambar).setOnClickListener { imagePickerLauncher.launch("image/*") }
        btnSimpan.setOnClickListener { if (validateInput()) { handleSave() } }
    }

    private fun initViews() {
        imagePreview = findViewById(R.id.iv_menu_image_preview)
        etNamaMenu = findViewById(R.id.et_nama_menu)
        etDetailMenu = findViewById(R.id.et_detail_menu)
        actvKategori = findViewById(R.id.actv_kategori)
        etHargaJual = findViewById(R.id.et_harga_jual)
        etHargaBeli = findViewById(R.id.et_harga_beli)
        etStokMenu = findViewById(R.id.et_stok_menu)
        switchStatus = findViewById(R.id.switch_status_menu)
        btnSimpan = findViewById(R.id.btn_simpan_menu)
    }

    private fun setupToolbar() {
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupCategoryDropdown() {
        val categories = listOf("Makanan", "Minuman", "Snack", "Kopi")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        actvKategori.setAdapter(adapter)
    }

    private fun loadMenuData() {
        db.collection("menus").document(menuId!!).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val menu = doc.toObject(Menu::class.java)
                    menu?.let { populateForm(it) }
                } else {
                    Toast.makeText(this, "Menu tidak ditemukan", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { Toast.makeText(this, "Gagal memuat data menu", Toast.LENGTH_SHORT).show() }
    }

    private fun populateForm(menu: Menu) {
        etNamaMenu.setText(menu.name)
        etHargaJual.setText(menu.price.toString())
        etDetailMenu.setText(menu.description) 
        etHargaBeli.setText(menu.priceBuy.toString()) 
        etStokMenu.setText(menu.stok.toString())
        actvKategori.setText(menu.kategori, false)
        switchStatus.isChecked = menu.status
        currentImageUrl = menu.imageUrl
        
        if (menu.imageUrl.isNotEmpty()) {
            Glide.with(this).load(menu.imageUrl).into(imagePreview)
        }
    }
    
    private fun validateInput(): Boolean {
        if (etNamaMenu.text.isNullOrEmpty() || etHargaJual.text.isNullOrEmpty() || etStokMenu.text.isNullOrEmpty()) {
            Toast.makeText(this, "Nama, Harga Jual, dan Stok tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return false
        }
        if (actvKategori.text.isNullOrEmpty()) {
            Toast.makeText(this, "Silakan pilih kategori menu", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun handleSave() {
        // PERBAIKAN 1: Gunakan ProgressDialog
        val progressDialog = ProgressDialog(this).apply {
            setMessage("Menyimpan perubahan...")
            setCancelable(false)
            show()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val imageUrl = if (imageUri != null) {
                    progressDialog.setMessage("Mengunggah gambar baru...")
                    contentResolver.openInputStream(imageUri!!)?.use { 
                        cloudinary.uploader().upload(it, mapOf("folder" to "borju_app/menus"))["secure_url"] as String 
                    }
                } else {
                    currentImageUrl
                }
                withContext(Dispatchers.Main) { 
                    if(imageUrl != null){
                        progressDialog.setMessage("Menyimpan data...")
                        updateMenuInFirestore(imageUrl, progressDialog)
                    } else {
                        throw IOException("Gagal mendapatkan URL gambar.")
                    }
                }

            } catch (e: Exception) { // PERBAIKAN 2: Tangkap semua jenis Exception
                 Log.e("FormEditMenu", "Save failed", e)
                 withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(this@FormEditMenu, "Proses simpan gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateMenuInFirestore(imageUrl: String, progressDialog: ProgressDialog) {
        // PERBAIKAN 3: Logika membuat data dipisahkan agar bersih
        val updatedData = mapOf(
            "name" to etNamaMenu.text.toString(),
            "price" to (etHargaJual.text.toString().toDoubleOrNull() ?: 0.0),
            "priceBuy" to (etHargaBeli.text.toString().toDoubleOrNull() ?: 0.0),
            "stok" to (etStokMenu.text.toString().toIntOrNull() ?: 0),
            "imageUrl" to imageUrl,
            "kategori" to actvKategori.text.toString(),
            "status" to switchStatus.isChecked,
            "description" to etDetailMenu.text.toString()
        )

        db.collection("menus").document(menuId!!)
            .update(updatedData)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Menu berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Gagal memperbarui database", Toast.LENGTH_LONG).show()
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}