package com.kp.borju_kp.admin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kp.borju_kp.R
import com.kp.borju_kp.admin.adapter.PengeluaranAdapter
import com.kp.borju_kp.data.Pengeluaran
import com.kp.borju_kp.utils.PdfExporter
import java.text.SimpleDateFormat
import java.util.*

class ManajemenPengeluaranActivity : AppCompatActivity() {

    private lateinit var pengeluaranAdapter: PengeluaranAdapter
    private val db = FirebaseFirestore.getInstance()
    private var fullPengeluaranList = listOf<Pengeluaran>()
    private val categories = listOf("Semua", "Bahan Baku", "Gaji", "Operasional", "Lainnya")

    private var startDate: Long? = null
    private var endDate: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manajemen_pengeluaran)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
        setupTabs()

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_tambah_pengeluaran).setOnClickListener {
            showAddPengeluaranDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        fetchPengeluaranData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_print, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_print -> {
                showPrintOptionsDialog()
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.rv_pengeluaran)
        recyclerView.layoutManager = LinearLayoutManager(this)
        pengeluaranAdapter = PengeluaranAdapter(listOf())
        recyclerView.adapter = pengeluaranAdapter
    }

    private fun fetchPengeluaranData() {
        db.collection("pengeluaran")
            .orderBy("tanggal", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val pengeluaranList = result.toObjects(Pengeluaran::class.java)
                fullPengeluaranList = pengeluaranList
                // Perbarui tampilan sesuai tab yang aktif
                val selectedTab = findViewById<TabLayout>(R.id.tab_layout_kategori_pengeluaran).getTabAt(
                    findViewById<TabLayout>(R.id.tab_layout_kategori_pengeluaran).selectedTabPosition
                )
                filterByCategory(selectedTab?.text.toString())
            }
            .addOnFailureListener { 
                Toast.makeText(this, "Gagal memuat data.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAddPengeluaranDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_tambah_pengeluaran, null)
        val etNama = dialogView.findViewById<EditText>(R.id.et_nama_pengeluaran)
        val etJumlah = dialogView.findViewById<EditText>(R.id.et_jumlah_pengeluaran)
        val actvKategori = dialogView.findViewById<AutoCompleteTextView>(R.id.actv_kategori_pengeluaran)

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories.filter { it != "Semua" })
        actvKategori.setAdapter(adapter)

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setNegativeButton("Batal", null)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = etNama.text.toString()
                val jumlah = etJumlah.text.toString().toDoubleOrNull()
                val kategori = actvKategori.text.toString()

                if (nama.isNotEmpty() && jumlah != null && kategori.isNotEmpty()) {
                    savePengeluaran(nama, jumlah, kategori)
                } else {
                    Toast.makeText(this, "Semua field tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun savePengeluaran(nama: String, jumlah: Double, kategori: String) {
        val newPengeluaran = Pengeluaran(namaPengeluaran = nama, jumlah = jumlah, kategori = kategori)
        db.collection("pengeluaran").add(newPengeluaran)
            .addOnSuccessListener {
                Toast.makeText(this, "Pengeluaran berhasil disimpan", Toast.LENGTH_SHORT).show()
                fetchPengeluaranData()
            }
            .addOnFailureListener { 
                Toast.makeText(this, "Gagal menyimpan pengeluaran", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupTabs() {
        val tabLayout: TabLayout = findViewById(R.id.tab_layout_kategori_pengeluaran)
        categories.forEach { tabLayout.addTab(tabLayout.newTab().setText(it)) }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) { filterByCategory(tab.text.toString()) }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun filterByCategory(category: String) {
        val filteredList = if (category == "Semua") {
            fullPengeluaranList
        } else {
            fullPengeluaranList.filter { it.kategori.equals(category, ignoreCase = true) }
        }
        pengeluaranAdapter.updateData(filteredList)
    }

    private fun showPrintOptionsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_opsi_cetak, null)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.rg_rentang_waktu)
        val datePickerLayout = dialogView.findViewById<LinearLayout>(R.id.layout_date_picker)
        val actvKategori = dialogView.findViewById<AutoCompleteTextView>(R.id.actv_kategori_cetak)

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        actvKategori.setAdapter(adapter)
        actvKategori.setText("Semua", false)

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            datePickerLayout.visibility = if (checkedId == R.id.rb_pilih_tanggal) View.VISIBLE else View.GONE
        }

        dialogView.findViewById<Button>(R.id.btn_tgl_mulai).setOnClickListener { showDatePicker { start, _ -> startDate = start } }
        dialogView.findViewById<Button>(R.id.btn_tgl_selesai).setOnClickListener { showDatePicker { _, end -> endDate = end } }

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setNegativeButton("Batal", null)
            .setPositiveButton("Export PDF") { _, _ ->
                if (checkStoragePermission()) {
                    exportToPdf(actvKategori.text.toString())
                }
            }
            .show()
    }

    private fun showDatePicker(onDateSelected: (Long, Long) -> Unit) {
        val picker = MaterialDatePicker.Builder.dateRangePicker().build()
        picker.show(supportFragmentManager, picker.toString())
        picker.addOnPositiveButtonClickListener {
            onDateSelected(it.first, it.second)
        }
    }

    private fun exportToPdf(selectedCategory: String) {
        val dataToExport = fullPengeluaranList.filter { pengeluaran ->
            val isInCategory = selectedCategory == "Semua" || pengeluaran.kategori.equals(selectedCategory, ignoreCase = true)
            val isInDateRange = if (startDate != null && endDate != null) {
                pengeluaran.tanggal != null && pengeluaran.tanggal!!.time >= startDate!! && pengeluaran.tanggal!!.time <= endDate!!
            } else {
                true
            }
            isInCategory && isInDateRange
        }

        if (dataToExport.isEmpty()) {
            Toast.makeText(this, "Tidak ada data untuk diexport", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val pdfFile = PdfExporter.createPengeluaranPdf(this, dataToExport, "Laporan Pengeluaran")
            Toast.makeText(this, "PDF berhasil disimpan di ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal membuat PDF: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun checkStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
                return false
            }
        }
        return true
    }
}