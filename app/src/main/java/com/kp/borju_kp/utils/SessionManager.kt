package com.kp.borju_kp.utils

import android.content.Context
import android.content.SharedPreferences
import com.kp.borju_kp.data.User

object SessionManager {

    private const val PREFS_NAME = "BorjuAppSession"
    private const val IS_LOGGED_IN = "isLoggedIn"
    private const val USER_ID = "userId"
    private const val USER_ROLE = "userRole"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(user: User) {
        val editor = prefs.edit()
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.putString(USER_ID, user.id_user)
        editor.putString(USER_ROLE, user.nama_role)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(IS_LOGGED_IN, false)
    }

    fun getUserId(): String? {
        return prefs.getString(USER_ID, null)
    }

    fun getUserRole(): String? {
        return prefs.getString(USER_ROLE, null)
    }

    fun logout() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}