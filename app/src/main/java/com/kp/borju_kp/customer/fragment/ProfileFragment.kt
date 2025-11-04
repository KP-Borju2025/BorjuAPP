package com.kp.borju_kp.customer.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.kp.borju_kp.MainActivity
import com.kp.borju_kp.R
import com.kp.borju_kp.customer.DashboardCostumer
import com.kp.borju_kp.customer.EditProfileCustomerActivity
import com.kp.borju_kp.utils.SessionManager

class ProfileFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private var userProfileListener: ListenerRegistration? = null

    private lateinit var profileImage: ShapeableImageView
    private lateinit var profileName: TextView
    private lateinit var profileEmail: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        try {
            SessionManager.init(requireContext())
        } catch (e: UninitializedPropertyAccessException) {
            // Already initialized, do nothing
        }

        profileImage = view.findViewById(R.id.iv_profile_picture)
        profileName = view.findViewById(R.id.tv_profile_name)
        profileEmail = view.findViewById(R.id.tv_profile_email)

        val btnEditProfile: TextView = view.findViewById(R.id.btn_edit_profile)
        val btnOrderHistory: TextView = view.findViewById(R.id.btn_order_history)
        val btnLogout: MaterialButton = view.findViewById(R.id.btn_logout)

        btnEditProfile.setOnClickListener {
            startActivity(Intent(activity, EditProfileCustomerActivity::class.java))
        }

        btnOrderHistory.setOnClickListener {
            // Berpindah ke tab riwayat (indeks 2)
            (activity as? DashboardCostumer)?.let {
                it.switchToTab(2)
            }
        }

        btnLogout.setOnClickListener {
            SessionManager.logout()
            Toast.makeText(context, "Logout berhasil", Toast.LENGTH_SHORT).show()
            val intent = Intent(activity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        listenToUserProfile()
    }

    override fun onStop() {
        super.onStop()
        userProfileListener?.remove()
    }

    private fun listenToUserProfile() {
        val userId = SessionManager.getUserId()
        Log.d("ProfileFragment", "Listening to profile for User ID: $userId")

        if (userId != null) {
            userProfileListener = db.collection("USER").document(userId)
                .addSnapshotListener { document, error ->
                    if (error != null) {
                        Log.e("ProfileFragment", "Listen failed.", error)
                        Toast.makeText(context, "Gagal memuat profil", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    if (document != null && document.exists()) {
                        Log.d("ProfileFragment", "DocumentSnapshot data: ${document.data}")

                        val name = document.getString("nama_user") ?: "Nama Pengguna"
                        val email = document.getString("email") ?: "email@example.com"
                        val imageUrl = document.getString("profile_image_url") ?: ""

                        profileName.text = name
                        profileEmail.text = email

                        if (isAdded) {
                            if (imageUrl.isNotEmpty()) {
                                Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .error(R.drawable.ic_launcher_background)
                                    .into(profileImage)
                            } else {
                                profileImage.setImageResource(R.drawable.ic_launcher_background)
                            }
                        }
                    } else {
                        Log.w("ProfileFragment", "No such document for user ID: $userId in 'USER' collection")
                        profileName.text = "Pengguna Tidak Ditemukan"
                        profileEmail.text = ""
                    }
                }
        } else {
            Log.w("ProfileFragment", "User ID is null. User might not be logged in.")
        }
    }
}
