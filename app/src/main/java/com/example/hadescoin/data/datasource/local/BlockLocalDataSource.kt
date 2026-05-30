package com.example.hadescoin.data.datasource.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Datasource local: único punto que toca SharedPreferences del bloqueo ATM.
 * Mismo patrón que SessionLocalDataSource en la app principal HadesCoin.
 * No contiene lógica de negocio, solo lectura/escritura de claves.
 */
class BlockLocalDataSource(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME           = "hadescoin_atm_block"
        private const val KEY_FAILED_ATTEMPTS  = "failed_attempts"
        private const val KEY_BLOCKED_UNTIL_MS = "blocked_until_ms"
    }

    fun getFailedAttempts(): Int  = prefs.getInt(KEY_FAILED_ATTEMPTS, 0)
    fun getBlockedUntilMs(): Long = prefs.getLong(KEY_BLOCKED_UNTIL_MS, 0L)

    fun saveFailedAttempts(value: Int) =
        prefs.edit().putInt(KEY_FAILED_ATTEMPTS, value).apply()

    fun saveBlockedUntilMs(value: Long) =
        prefs.edit().putLong(KEY_BLOCKED_UNTIL_MS, value).apply()

    fun clear() {
        prefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_BLOCKED_UNTIL_MS, 0L)
            .apply()
    }
}
