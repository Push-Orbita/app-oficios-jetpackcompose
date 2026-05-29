package com.cocido.oficios.core.data.logging

import android.util.Log
import com.cocido.oficios.core.common.logging.AppLogger

class AndroidAppLogger : AppLogger {
    override fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
}
