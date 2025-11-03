package com.kp.borju_kp.customer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.kp.borju_kp.CloudinaryConfig
import com.kp.borju_kp.R
import com.kp.borju_kp.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileCustomerActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var profileImage: ShapeableImageView
    private lateinit var btnChangePhoto: TextView
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var btnSave: Button

    private val db = FirebaseFirestore.getInstance()
    private var imageUri: Uri? = null
    private var currentImageUrl: String? = null
    private lateinit var userId: String

    private companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile_customer)

        toolbar = findViewById(R.id.toolbar)
        profileImage = findViewById(R.id.iv_edit_profile_image)
        btnChangePhoto = findViewById(R.id.btn_change_photo)
        etName = findViewById(R.id.et_edit_name)
        etEmail = findViewById(R.id.et_edit_email)
        etPhone = findViewById(R.id.et_edit_phone)
        etAddress = findViewById(R.id.et_edit_address)
        btnSave = findViewById(R.id.btn_save_profile)

        setupToolbar()

        SessionManager.init(this)
        userId = SessionManager.getUserId().toString()

        loadUserData()

        btnChangePhoto.setOnClickListener {
            openFileChooser()
        }

        btnSave.setOnClickListener {
            saveProfileChanges()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            profileImage.setImageURI(imageUri)
        }
    }

    private fun loadUserData() {
        if (userId.isEmpty()) return

        db.collection("USER").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    etName.setText(document.getString("nama_user"))
                    etEmail.setText(document.getString("email"))
                    etPhone.setText(document.getString("nohp"))
                    etAddress.setText(document.getString("alamat"))
                    currentImageUrl = document.getString("profile_image_url")

                    if (!currentImageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(currentImageUrl).into(profileImage)
                    }
                }
            }
    }

    private fun saveProfileChanges() {
        val name = etName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val address = etAddress.text.toString().trim()

        if (name.isEmpty()) {
            etName.error = "Nama tidak boleh kosong"
            return
        }

        if (imageUri != null) {
            uploadImageToCloudinary(name, phone, address)
        } else {
            updateUserInFirestore(name, phone, address, currentImageUrl)
        }
    }

    private fun uploadImageToCloudinary(name: String, phone: String, address: String) {
        imageUri?.let { uri ->
            Toast.makeText(this, "Mengunggah foto...", Toast.LENGTH_SHORT).show()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val uploadResult = CloudinaryConfig.instance.uploader().upload(inputStream?.readBytes(), null)
                    val newImageUrl = uploadResult["secure_url"] as? String

                    withContext(Dispatchers.Main) {
                        updateUserInFirestore(name, phone, address, newImageUrl)
                    }
                } catch (e: Exception) {
                    Log.e("CloudinaryError", "Upload failed", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@EditProfileCustomerActivity, "Gagal mengunggah foto", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateUserInFirestore(name: String, phone: String, address: String, imageUrl: String?) {
        val userUpdates = mapOf(
            "nama_user" to name,
            "nohp" to phone,
            "alamat" to address,
            "profile_image_url" to (imageUrl ?: "")
        )

        db.collection("USER").document(userId).update(userUpdates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memperbarui profil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}