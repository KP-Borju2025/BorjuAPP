package com.kp.borju_kp.customer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kp.borju_kp.R
import com.kp.borju_kp.data.Menu
import com.kp.borju_kp.viewmodel.OrderViewModel

class MenuDetailBottomSheetFragment : BottomSheetDialogFragment() {

    private val orderViewModel: OrderViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_menu_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menu = arguments?.getParcelable<Menu>(ARG_MENU)

        menu?.let {
            displayMenuDetails(it)
            setupAddToCartButton(it)
        }


    }

    private fun displayMenuDetails(menu: Menu) {
        view?.apply {
            findViewById<TextView>(R.id.tv_detail_menu_name).text = menu.name
            findViewById<TextView>(R.id.tv_detail_menu_price).text = "Rp ${menu.price.toInt()}"
            findViewById<TextView>(R.id.tv_detail_menu_description).text = menu.description

            val imageView = findViewById<ImageView>(R.id.iv_detail_menu_image)
            Glide.with(this)
                .load(menu.imageUrl)
                .into(imageView)
        }
    }

    private fun setupAddToCartButton(menu: Menu) {
        val btnAddToCart = view?.findViewById<Button>(R.id.btn_add_to_cart_detail)
        // PERBAIKAN: Tambahkan pemeriksaan untuk menu.status
        if (menu.status && menu.stok > 0) {
            btnAddToCart?.text = "Tambah ke Keranjang"
            btnAddToCart?.isEnabled = true
            btnAddToCart?.setOnClickListener {
                // Periksa lagi di sini sebagai jaring pengaman terakhir
                if (menu.status && menu.stok > 0) {
                    orderViewModel.addItem(menu)
                    Toast.makeText(context, "${menu.name} ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    Toast.makeText(context, "Maaf, menu ini sudah tidak tersedia", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Tampilkan pesan yang sesuai berdasarkan penyebab ketidaktersediaan
            btnAddToCart?.text = if (!menu.status) "Tidak Tersedia" else "Stok Habis"
            btnAddToCart?.isEnabled = false
        }
    }

    companion object {
        const val TAG = "MenuDetailBottomSheetFragment"
        private const val ARG_MENU = "menu_arg"

        fun newInstance(menu: Menu): MenuDetailBottomSheetFragment {
            val fragment = MenuDetailBottomSheetFragment()
            val args = Bundle()
            args.putParcelable(ARG_MENU, menu)
            fragment.arguments = args
            return fragment
        }
    }
}