package com.kp.borju_kp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kp.borju_kp.data.CartItem
import com.kp.borju_kp.data.Menu

class OrderViewModel : ViewModel() {

    private val _cartItems = MutableLiveData<MutableList<CartItem>>(mutableListOf())
    val cartItems: LiveData<MutableList<CartItem>> get() = _cartItems

    fun addItem(menu: Menu) {
        val cart = _cartItems.value ?: mutableListOf()
        val existingItem = cart.find { it.menu.id == menu.id }

        if (existingItem != null) {
            existingItem.quantity++
        } else {
            cart.add(CartItem(menu = menu, quantity = 1))
        }
        _cartItems.value = cart
    }

    fun removeItem(cartItem: CartItem) {
        val cart = _cartItems.value ?: return
        cart.remove(cartItem)
        _cartItems.value = cart
    }

    fun increaseQuantity(cartItem: CartItem) {
        cartItem.quantity++
        _cartItems.value = _cartItems.value
    }

    fun decreaseQuantity(cartItem: CartItem) {
        if (cartItem.quantity > 1) {
            cartItem.quantity--
        } else {
            removeItem(cartItem)
        }
        _cartItems.value = _cartItems.value
    }

    fun updateNote(cartItem: CartItem, note: String) {
        cartItem.note = note
        _cartItems.value = _cartItems.value // Trigger update to refresh UI
    }

    fun getTotalPrice(): Double {
        return _cartItems.value?.sumOf { it.menu.price * it.quantity } ?: 0.0
    }

    fun clearCart() {
        _cartItems.value = mutableListOf()
    }
}