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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.kp.borju_kp.CloudinaryConfig
import com.kp.borju_kp.R
import com.kp.borju_kp.data.Menu
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FormTambahMenu : AppCompatActivity() {

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

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            Glide.with(this).load(it).into(imagePreview)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_tambah_menu)

        setupToolbar()
        initViews()
        setupCategoryDropdown()

        findViewById<Button>(R.id.btn_pilih_gambar).setOnClickListener { imagePickerLauncher.launch("image/*") }
        btnSimpan.setOnClickListener { if (validateInput()) { uploadImageAndSaveData() } }
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

    private fun validateInput(): Boolean {
        if (imageUri == null) {
            Toast.makeText(this, "Silakan pilih gambar menu terlebih dahulu", Toast.LENGTH_SHORT).show()
            return false
        }
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

    private fun uploadImageAndSaveData() {
        val progressDialog = ProgressDialog(this).apply {
            setMessage("Mengunggah gambar...")
            setCancelable(false)
            show()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val imageUrl = contentResolver.openInputStream(imageUri!!)?.use { inputStream ->
                    cloudinary.uploader().upload(inputStream, mapOf("folder" to "borju_app/menus"))["secure_url"] as String
                }
                withContext(Dispatchers.Main) {
                    if (imageUrl != null) {
                        progressDialog.setMessage("Menyimpan data...")
                        
                        // PERBAIKAN: Buat objek Menu di sini
                        val newMenu = Menu(
                            name = etNamaMenu.text.toString(),
                            price = etHargaJual.text.toString().toDoubleOrNull() ?: 0.0,
                            priceBuy = etHargaBeli.text.toString().toDoubleOrNull() ?: 0.0,
                            stok = etStokMenu.text.toString().toIntOrNull() ?: 0,
                            imageUrl = imageUrl,
                            kategori = actvKategori.text.toString(),
                            status = switchStatus.isChecked,
                            description = etDetailMenu.text.toString()
                        )
                        
                        // Kirim objek yang sudah jadi untuk disimpan
                        saveMenuToFirestore(newMenu, progressDialog)
                    } else {
                        throw IOException("Gagal mendapatkan URL gambar dari Cloudinary")
                    }
                }
            } catch (e: Exception) {
                Log.e("FormTambahMenu", "Upload failed", e)
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(this@FormTambahMenu, "Proses gagal: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun saveMenuToFirestore(menu: Menu, progressDialog: ProgressDialog) {
        // PERBAIKAN: Fungsi ini sekarang hanya bertanggung jawab untuk menyimpan
        db.collection("menus").add(menu)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Menu baru berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Gagal menyimpan ke database: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}