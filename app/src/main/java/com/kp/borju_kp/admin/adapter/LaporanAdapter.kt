package com.kp.borju_kp.admin.adapter

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kp.borju_kp.R
import com.kp.borju_kp.data.TipeTransaksi
import com.kp.borju_kp.data.Transaksi
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class LaporanAdapter(private var transaksiList: List<Transaksi>) : RecyclerView.Adapter<LaporanAdapter.TransaksiViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransaksiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaksi, parent, false)
        return TransaksiViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransaksiViewHolder, position: Int) {
        holder.bind(transaksiList[position])
    }

    override fun getItemCount(): Int = transaksiList.size

    fun updateData(newTransaksiList: List<Transaksi>) {
        this.transaksiList = newTransaksiList
        notifyDataSetChanged()
    }

    fun getItems(): List<Transaksi> {
        return transaksiList
    }

    class TransaksiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deskripsi: TextView = itemView.findViewById(R.id.tv_transaksi_deskripsi)
        private val jumlah: TextView = itemView.findViewById(R.id.tv_transaksi_jumlah)
        private val tanggal: TextView = itemView.findViewById(R.id.tv_transaksi_tanggal)

        fun bind(transaksi: Transaksi) {
            deskripsi.text = transaksi.deskripsi

            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            tanggal.text = sdf.format(transaksi.tanggal)

            val formattedAmount = formatCurrency(transaksi.jumlah)

            if (transaksi.tipe == TipeTransaksi.PEMASUKAN) {
                jumlah.text = "+ ${formattedAmount}"
                // PERBAIKAN: Menggunakan atribut tema aplikasi
                jumlah.setTextColor(getThemeColor(itemView.context, com.google.android.material.R.attr.colorTertiary))
            } else {
                jumlah.text = "- ${formattedAmount}"
                // PERBAIKAN: Menggunakan atribut tema aplikasi
                jumlah.setTextColor(getThemeColor(itemView.context, androidx.appcompat.R.attr.colorError))
            }
        }

        private fun formatCurrency(amount: Double): String {
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            format.maximumFractionDigits = 0
            return format.format(amount)
        }

        private fun getThemeColor(context: Context, colorAttr: Int): Int {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(colorAttr, typedValue, true)
            return typedValue.data
        }
    }
}