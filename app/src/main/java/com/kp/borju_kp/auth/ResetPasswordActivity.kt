package com.kp.borju_kp.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.kp.borju_kp.databinding.ActivityResetPasswordBinding

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()


        auth = FirebaseAuth.getInstance()

        // Listener Tombol Kirim Tautan
        binding.btnSendReset.setOnClickListener {
            sendResetLink()
        }

        // Listener Kembali ke Login
        binding.tvBackToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun sendResetLink() {
        val email = binding.etResetEmail.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Mohon masukkan Email terdaftar.", Toast.LENGTH_SHORT).show()
            return
        }

        // Kirim tautan reset ke Firebase
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Tautan reset sandi telah dikirim ke $email. Cek email Anda!",
                        Toast.LENGTH_LONG
                    ).show()

                    // Kembali ke Login setelah sukses
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    val errorMessage = task.exception?.message
                    Toast.makeText(this, "Gagal. Pastikan email terdaftar. (${errorMessage})", Toast.LENGTH_LONG).show()
                }
            }
    }
}