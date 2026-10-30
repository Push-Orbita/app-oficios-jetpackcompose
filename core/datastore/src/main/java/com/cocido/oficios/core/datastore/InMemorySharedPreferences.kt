package com.cocido.oficios.core.datastore

import android.content.SharedPreferences

/** Fallback sin persistencia para cuando el Keystore no permite cifrar la sesión. */
internal class InMemorySharedPreferences : SharedPreferences {

    private val values = mutableMapOf<String, Any?>()
    private val listeners = mutableSetOf<SharedPreferences.OnSharedPreferenceChangeListener>()

    override fun getAll(): MutableMap<String, *> = synchronized(values) { values.toMutableMap() }

    override fun getString(key: String?, defValue: String?): String? = read(key, defValue)

    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
        (synchronized(values) { values[key] } as? Set<String>)?.toMutableSet() ?: defValues

    override fun getInt(key: String?, defValue: Int): Int = read(key, defValue)

    override fun getLong(key: String?, defValue: Long): Long = read(key, defValue)

    override fun getFloat(key: String?, defValue: Float): Float = read(key, defValue)

    override fun getBoolean(key: String?, defValue: Boolean): Boolean = read(key, defValue)

    override fun contains(key: String?): Boolean = synchronized(values) { values.containsKey(key) }

    override fun edit(): SharedPreferences.Editor = Editor()

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener,
    ) {
        synchronized(listeners) { listeners += listener }
    }

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener,
    ) {
        synchronized(listeners) { listeners -= listener }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> read(key: String?, defValue: T): T =
        synchronized(values) { values[key] as? T } ?: defValue

    private inner class Editor : SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Any?>()
        private val removals = mutableSetOf<String>()
        private var clearAll = false

        override fun putString(key: String, value: String?) = put(key, value)
        override fun putStringSet(key: String, values: MutableSet<String>?) = put(key, values?.toSet())
        override fun putInt(key: String, value: Int) = put(key, value)
        override fun putLong(key: String, value: Long) = put(key, value)
        override fun putFloat(key: String, value: Float) = put(key, value)
        override fun putBoolean(key: String, value: Boolean) = put(key, value)

        override fun remove(key: String): SharedPreferences.Editor = apply {
            pending.remove(key)
            removals += key
        }

        override fun clear(): SharedPreferences.Editor = apply { clearAll = true }

        override fun commit(): Boolean {
            val changed = mutableSetOf<String>()
            synchronized(values) {
                if (clearAll) {
                    changed += values.keys
                    values.clear()
                }
                removals.forEach { key -> if (values.containsKey(key)) { values.remove(key); changed += key } }
                pending.forEach { (key, value) ->
                    if (value == null) values.remove(key) else values[key] = value
                    changed += key
                }
            }
            val snapshot = synchronized(listeners) { listeners.toList() }
            changed.forEach { key -> snapshot.forEach { it.onSharedPreferenceChanged(this@InMemorySharedPreferences, key) } }
            return true
        }

        override fun apply() {
            commit()
        }

        private fun put(key: String, value: Any?): SharedPreferences.Editor = apply {
            removals -= key
            pending[key] = value
        }
    }
}
