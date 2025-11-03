package com.kp.borju_kp.customer.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kp.borju_kp.R
import com.kp.borju_kp.customer.CustomerCheckoutActivity
import com.kp.borju_kp.customer.adapter.CartAdapter
import com.kp.borju_kp.data.CartItem
import com.kp.borju_kp.viewmodel.OrderViewModel

class CartBottomSheetFragment : BottomSheetDialogFragment() {

    private val orderViewModel: OrderViewModel by activityViewModels()
    private lateinit var cartAdapter: CartAdapter
    private lateinit var rvCartItems: RecyclerView
    private lateinit var tvTotalPrice: TextView
    private lateinit var tvEmptyCart: TextView
    private lateinit var btnCheckout: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart_bottom_sheet, container, false)

        rvCartItems = view.findViewById(R.id.rv_cart_items)
        tvTotalPrice = view.findViewById(R.id.tv_total_price)
        tvEmptyCart = view.findViewById(R.id.tv_empty_cart)
        btnCheckout = view.findViewById(R.id.btn_checkout)

        setupRecyclerView()
        observeViewModel()

        btnCheckout.setOnClickListener {
            if (orderViewModel.cartItems.value.isNullOrEmpty()) {
                Toast.makeText(context, "Keranjang kosong", Toast.LENGTH_SHORT).show()
            } else {
                // Buka halaman checkout dan kirim data
                val intent = Intent(activity, CustomerCheckoutActivity::class.java).apply {
                    putParcelableArrayListExtra("cart_items", ArrayList(orderViewModel.cartItems.value!!))
                    putExtra("total_price", orderViewModel.getTotalPrice())
                }
                startActivity(intent)
                dismiss() // Menutup bottom sheet setelah membuka checkout
            }
        }

        return view
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            mutableListOf(),
            onIncrease = { cartItem -> orderViewModel.increaseQuantity(cartItem) },
            onDecrease = { cartItem -> orderViewModel.decreaseQuantity(cartItem) },
            onDelete = { cartItem -> orderViewModel.removeItem(cartItem) },
            onNoteClick = { cartItem -> showNoteDialog(cartItem) }
        )
        rvCartItems.layoutManager = LinearLayoutManager(context)
        rvCartItems.adapter = cartAdapter
    }

    private fun observeViewModel() {
        orderViewModel.cartItems.observe(viewLifecycleOwner) { cartItems ->
            if (cartItems.isNullOrEmpty()) {
                tvEmptyCart.visibility = View.VISIBLE
                rvCartItems.visibility = View.GONE
            } else {
                tvEmptyCart.visibility = View.GONE
                rvCartItems.visibility = View.VISIBLE
                cartAdapter.updateData(cartItems)
            }
            tvTotalPrice.text = "Rp ${orderViewModel.getTotalPrice().toInt()}"
            btnCheckout.isEnabled = !cartItems.isNullOrEmpty()
        }
    }

    private fun showNoteDialog(cartItem: CartItem) {
        val editText = EditText(context).apply {
            setText(cartItem.note)
            hint = "Contoh: Jangan pakai bawang"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Tambah Catatan")
            .setView(editText)
            .setPositiveButton("Simpan") { dialog, _ ->
                val note = editText.text.toString()
                orderViewModel.updateNote(cartItem, note)
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    companion object {
        const val TAG = "CartBottomSheetFragment"
    }
}