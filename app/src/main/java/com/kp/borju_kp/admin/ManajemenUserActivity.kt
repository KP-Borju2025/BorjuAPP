package com.kp.borju_kp.admin

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kp.borju_kp.R
import com.kp.borju_kp.admin.adapter.UserAdapter
import com.kp.borju_kp.data.User

class ManajemenUserActivity : AppCompatActivity() {

    private lateinit var userAdapter: UserAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manajeman_user)

        setupToolbar()
        setupRecyclerView()
        fetchUsersData()

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_add_user).setOnClickListener {
            Toast.makeText(this, "Buka form tambah user", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupToolbar() {
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.rv_users)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(listOf())
        recyclerView.adapter = userAdapter
    }

    private fun fetchUsersData() {
        // PERBAIKAN: Menggunakan nama koleksi "USER" dan mengurutkan berdasarkan "nama_role"
        db.collection("USER") 
            .orderBy("nama_role", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(this, "Tidak ada data user.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                val userList = result.map { document ->
                    val user = document.toObject(User::class.java)
                    user.uid = document.id // Simpan ID dokumen ke properti uid
                    user
                }
                userAdapter.updateData(userList)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat data pengguna: ${e.message}", Toast.LENGTH_LONG).show()
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