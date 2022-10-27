package com.elmansoft.elmancall

import android.telecom.Call

class CallManager private constructor() {

    var call: Call? = null

    companion object {
        @Volatile private var instance: CallManager? = null

        @JvmStatic fun getInstance(): CallManager =
            instance ?: synchronized(this) {
                instance ?: CallManager().also {
                    instance = it
                }
            }
    }

    fun updateCall(call: Call?) {
        this.call = call
    }
}