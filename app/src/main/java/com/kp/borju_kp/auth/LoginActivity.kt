package com.kp.borju_kp.auth

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kp.borju_kp.R
import com.kp.borju_kp.admin.DashboardAdmin
import com.kp.borju_kp.customer.DashboardCostumer

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        enableEdgeToEdge()


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val tvGoRegister = findViewById<TextView>(R.id.tv_go_register)
        val tvResetPassword = findViewById<TextView>(R.id.tv_reset_password)

        // Tombol Login
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            // Ambil role dari Firestore
                            db.collection("USER").document(userId).get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        val role = document.getString("nama_role")
                                        when (role) {
                                            "Admin" -> {
                                                Toast.makeText(this, "Login sebagai Admin", Toast.LENGTH_SHORT).show()
                                                startActivity(Intent(this, DashboardAdmin::class.java))
                                                finish()
                                            }
                                            "Customer" -> {
                                                Toast.makeText(this, "Login sebagai Customer", Toast.LENGTH_SHORT).show()
                                                startActivity(Intent(this, DashboardCostumer::class.java))
                                                finish()
                                            }
                                            else -> {
                                                Toast.makeText(this, "Role tidak dikenali!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(this, "Data user tidak ditemukan!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Gagal ambil data user: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Login gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Tombol ke halaman register
        tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Tombol Reset Password
        tvResetPassword.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }
    }
}
