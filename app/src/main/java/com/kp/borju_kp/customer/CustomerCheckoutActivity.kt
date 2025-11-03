package com.kp.borju_kp.customer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.kp.borju_kp.R
import com.kp.borju_kp.customer.adapter.CheckoutAdapter
import com.kp.borju_kp.data.CartItem
import com.kp.borju_kp.utils.SessionManager
import java.util.Locale

class CustomerCheckoutActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var rvCheckoutItems: RecyclerView
    private lateinit var etAddress: TextInputEditText
    private lateinit var btnGetCurrentLocation: Button
    private lateinit var tvTotalPrice: TextView
    private lateinit var rgPaymentMethod: RadioGroup
    private lateinit var btnPlaceOrder: Button

    private var cartItems: ArrayList<CartItem>? = null
    private var totalPrice: Double = 0.0
    private var customerName: String = ""

    private val db = FirebaseFirestore.getInstance()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_checkout)

        toolbar = findViewById(R.id.toolbar)
        rvCheckoutItems = findViewById(R.id.rv_checkout_items)
        etAddress = findViewById(R.id.et_checkout_address)
        btnGetCurrentLocation = findViewById(R.id.btn_get_current_location)
        tvTotalPrice = findViewById(R.id.tv_checkout_total_price)
        rgPaymentMethod = findViewById(R.id.rg_payment_method)
        btnPlaceOrder = findViewById(R.id.btn_place_order)

        setupToolbar()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        cartItems = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("cart_items", CartItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra("cart_items")
        }
        totalPrice = intent.getDoubleExtra("total_price", 0.0)

        if (cartItems != null) {
            setupRecyclerView()
            tvTotalPrice.text = "Rp ${totalPrice.toInt()}"
        } else {
            Toast.makeText(this, "Keranjang kosong!", Toast.LENGTH_SHORT).show()
            finish()
        }

        loadUserData()

        btnGetCurrentLocation.setOnClickListener {
            checkLocationPermission()
        }

        btnPlaceOrder.setOnClickListener {
            placeOrder()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupRecyclerView() {
        rvCheckoutItems.layoutManager = LinearLayoutManager(this)
        rvCheckoutItems.adapter = CheckoutAdapter(cartItems!!)
    }

    private fun loadUserData(){
        val userId = SessionManager.getUserId()
        if (userId != null) {
            db.collection("USER").document(userId).get().addOnSuccessListener {
                if(it != null && it.exists()){
                    customerName = it.getString("nama_user") ?: ""
                    val defaultAddress = it.getString("alamat") ?: ""
                    if(etAddress.text.isNullOrEmpty()) {
                        etAddress.setText(defaultAddress)
                    }
                }
            }
        }
    }

    private fun placeOrder() {
        val address = etAddress.text.toString().trim()
        if (address.isEmpty()) {
            etAddress.error = "Alamat tidak boleh kosong"
            etAddress.requestFocus()
            return
        }

        val selectedPaymentMethodId = rgPaymentMethod.checkedRadioButtonId
        val paymentMethod = when (selectedPaymentMethodId) {
            R.id.rb_cod -> "COD (Bayar di Tempat)"
            R.id.rb_qris -> "QRIS"
            R.id.rb_transfer -> "Transfer Bank"
            else -> "N/A"
        }

        // Jika COD, langsung simpan pesanan
        if (paymentMethod == "COD (Bayar di Tempat)") {
            saveCodOrder(address)
        } else {
            // Jika bukan COD, buka halaman konfirmasi pembayaran
            navigateToPaymentConfirmation(address, paymentMethod)
        }
    }

    private fun saveCodOrder(address: String) {
        val orderData = createOrderData(address, "COD (Bayar di Tempat)")
        db.collection("orders").add(orderData)
            .addOnSuccessListener {
                Toast.makeText(this, "Pesanan COD berhasil dibuat!", Toast.LENGTH_SHORT).show()
                navigateToHome()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal membuat pesanan COD", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToPaymentConfirmation(address: String, paymentMethod: String) {
        val orderData = createOrderData(address, paymentMethod)
        val intent = Intent(this, PaymentConfirmationActivity::class.java).apply {
            putExtra("order_data", orderData)
        }
        startActivity(intent)
    }

    private fun createOrderData(address: String, paymentMethod: String): HashMap<String, Any> {
        val userId = SessionManager.getUserId() ?: ""

        val itemsForFirestore = cartItems?.map { cartItem ->
            mapOf(
                "menuId" to cartItem.menu.id,
                "menuName" to cartItem.menu.name,
                "quantity" to cartItem.quantity,
                "price" to cartItem.menu.price,
                "note" to cartItem.note
            )
        }

        return hashMapOf(
            "customerId" to userId,
            "customerName" to customerName,
            "shippingAddress" to address,
            "items" to (itemsForFirestore ?: emptyList()),
            "orderTimestamp" to Timestamp.now(),
            "totalPrice" to totalPrice,
            "paymentMethod" to paymentMethod,
            "orderType" to "Online",
            "status" to if (paymentMethod == "COD (Bayar di Tempat)") "Menunggu Konfirmasi" else "Menunggu Pembayaran"
        )
    }

    private fun navigateToHome() {
        // TODO: Panggil viewModel.clearCart() di sini jika sudah ada
        val intent = Intent(this, DashboardCostumer::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // ... (Fungsi lokasi tetap sama)
    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getCurrentLocation() {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        try {
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            if (addresses != null && addresses.isNotEmpty()) {
                                val address = addresses[0]
                                val addressText = address.getAddressLine(0)
                                etAddress.setText(addressText)
                            }
                        } catch (e: Exception) {
                            Log.e("Geocoder", "Error getting address", e)
                        }
                    } else {
                        Toast.makeText(this, "Gagal mendapatkan lokasi. Pastikan GPS aktif.", Toast.LENGTH_LONG).show()
                    }
                }
        } catch (e: SecurityException) {
            Log.e("Location", "Security exception", e)
        }
    }
}