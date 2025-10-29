package com.kp.borju_kp.customer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.kp.borju_kp.R

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val tvProfileName: TextView = view.findViewById(R.id.tv_profile_name)
        val tvProfileEmail: TextView = view.findViewById(R.id.tv_profile_email)
        val btnLogout: Button = view.findViewById(R.id.btn_logout)

        // Dummy data
        tvProfileName.text = "John Doe"
        tvProfileEmail.text = "john.doe@example.com"

        btnLogout.setOnClickListener {
            Toast.makeText(context, "Logout berhasil", Toast.LENGTH_SHORT).show()
            // Tambahkan logika untuk kembali ke halaman login di sini
        }

        return view
    }
}