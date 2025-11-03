package com.kp.borju_kp.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kp.borju_kp.R
import com.kp.borju_kp.auth.LoginActivity
import com.kp.borju_kp.data.User
import com.kp.borju_kp.utils.SessionManager

class ProfileAdminActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var ivProfile: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvAddress: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnLogout: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_admin)
        enableEdgeToEdge()

        // Inisialisasi Views
        toolbar = findViewById(R.id.toolbar_profile)
        ivProfile = findViewById(R.id.iv_profile_image)
        tvName = findViewById(R.id.tv_profile_name)
        tvEmail = findViewById(R.id.tv_profile_email)
        tvPhone = findViewById(R.id.tv_profile_phone)
        tvAddress = findViewById(R.id.tv_profile_address)
        btnEdit = findViewById(R.id.btn_edit_profile)
        btnLogout = findViewById(R.id.btn_logout)

        setupToolbar()

        btnEdit.setOnClickListener {
            startActivity(Intent(this, EditProfileAdminActivity::class.java))
        }

        btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Pengguna tidak ditemukan, silakan login kembali", Toast.LENGTH_LONG).show()
            logoutUser()
            return
        }

        db.collection("USER").document(userId).get().addOnSuccessListener {
            val user = it.toObject(User::class.java)
            if (user != null) {
                tvName.text = user.nama_user
                tvEmail.text = user.email
                tvPhone.text = user.nohp
                tvAddress.text = user.alamat

                if (user.profile_image_url.isNotEmpty()) {
                    Glide.with(this).load(user.profile_image_url).circleCrop().into(ivProfile)
                }
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun logoutUser() {
        SessionManager.logout()
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}