package com.kp.borju_kp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        enableEdgeToEdge()


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Ambil komponen dari layout
        val etNama = findViewById<EditText>(R.id.et_nama)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val etNoHp = findViewById<EditText>(R.id.et_nohp)
        val etAlamat = findViewById<EditText>(R.id.et_alamat)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val tvGoLogin = findViewById<TextView>(R.id.tv_go_login)

        btnRegister.setOnClickListener {
            val nama = etNama.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val nohp = etNoHp.text.toString().trim()
            val alamat = etAlamat.text.toString().trim()

            if (nama.isEmpty() || email.isEmpty() || password.isEmpty() || nohp.isEmpty() || alamat.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Buat akun di Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                        // Simpan data user di Firestore
                        val userMap = hashMapOf(
                            "id_user" to userId,
                            "nama_user" to nama,
                            "email" to email,
                            "nohp" to nohp,
                            "alamat" to alamat,
                            "id_role" to "2", // 1 = Admin, 2 = Customer
                            "nama_role" to "Customer"
                        )

                        db.collection("USER").document(userId)
                            .set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                                finish() // kembali ke halaman login
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Gagal simpan data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Gagal register: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Arahkan ke halaman login
        tvGoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
