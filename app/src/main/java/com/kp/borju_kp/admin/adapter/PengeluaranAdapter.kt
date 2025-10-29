package com.kp.borju_kp.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kp.borju_kp.R
import com.kp.borju_kp.data.Pengeluaran
import java.text.SimpleDateFormat
import java.util.Locale

class PengeluaranAdapter(private var pengeluaranList: List<Pengeluaran>) : RecyclerView.Adapter<PengeluaranAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pengeluaran, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pengeluaran = pengeluaranList[position]
        holder.bind(pengeluaran)
    }

    override fun getItemCount(): Int = pengeluaranList.size

    fun updateData(newPengeluaranList: List<Pengeluaran>) {
        pengeluaranList = newPengeluaranList
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nama: TextView = itemView.findViewById(R.id.tv_nama_pengeluaran)
        private val tanggal: TextView = itemView.findViewById(R.id.tv_tanggal_pengeluaran)
        private val jumlah: TextView = itemView.findViewById(R.id.tv_jumlah_pengeluaran)

        fun bind(pengeluaran: Pengeluaran) {
            nama.text = pengeluaran.namaPengeluaran
            
            val formatter = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
            tanggal.text = pengeluaran.tanggal?.let { formatter.format(it) } ?: "-"
            
            jumlah.text = "- Rp ${String.format("%,.0f", pengeluaran.jumlah)}"
        }
    }
}