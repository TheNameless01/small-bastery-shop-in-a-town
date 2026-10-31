package com.example.security

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

class PinManager(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        "khoubza_security_prefs",
        Context.MODE_PRIVATE
    )

    private val KEY_PIN_HASH = "owner_pin_hash"
    private val KEY_PIN_SALT = "owner_pin_salt"
    private val KEY_PIN_ENABLED = "owner_pin_enabled"

    private val _isPinEnabled = MutableStateFlow(prefs.getBoolean(KEY_PIN_ENABLED, false))
    val isPinEnabled: StateFlow<Boolean> = _isPinEnabled.asStateFlow()

    fun hasPinConfigured(): Boolean {
        return prefs.getBoolean(KEY_PIN_ENABLED, false) && !prefs.getString(KEY_PIN_HASH, null).isNullOrEmpty()
    }

    fun setPin(newPin: String): Boolean {
        if (newPin.length < 4) return false
        val salt = System.currentTimeMillis().toString()
        val hash = hashPin(newPin, salt)
        prefs.edit()
            .putString(KEY_PIN_HASH, hash)
            .putString(KEY_PIN_SALT, salt)
            .putBoolean(KEY_PIN_ENABLED, true)
            .apply()
        _isPinEnabled.value = true
        return true
    }

    fun verifyPin(enteredPin: String): Boolean {
        val enabled = prefs.getBoolean(KEY_PIN_ENABLED, false)
        if (!enabled) return true
        val savedHash = prefs.getString(KEY_PIN_HASH, null) ?: return true
        val salt = prefs.getString(KEY_PIN_SALT, "") ?: ""
        val enteredHash = hashPin(enteredPin, salt)
        return savedHash == enteredHash
    }

    fun disablePin(currentPin: String): Boolean {
        if (!verifyPin(currentPin)) return false
        prefs.edit()
            .putBoolean(KEY_PIN_ENABLED, false)
            .remove(KEY_PIN_HASH)
            .remove(KEY_PIN_SALT)
            .apply()
        _isPinEnabled.value = false
        return true
    }

    private fun hashPin(pin: String, salt: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest((pin + salt).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
