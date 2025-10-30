package com.kp.borju_kp.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kp.borju_kp.R
import com.kp.borju_kp.data.User

class UserAdapter(private var userList: List<User>) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount(): Int = userList.size

    fun updateData(newUserList: List<User>) {
        userList = newUserList
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.tv_user_name)
        private val email: TextView = itemView.findViewById(R.id.tv_user_email)
        private val role: TextView = itemView.findViewById(R.id.tv_user_role)

        fun bind(user: User) {
            name.text = user.name
            email.text = user.email
            role.text = user.role.replaceFirstChar { it.uppercase() } // Membuat huruf pertama kapital
        }
    }
}