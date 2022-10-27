package com.elmansoft.elmancall

import android.content.Intent
import android.content.Intent.*
import android.os.Handler
import android.os.ResultReceiver
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.elmansoft.elmancall.ui.incoming.IncomingCallActivity


class CallService : InCallService() {

    var broadcaster: LocalBroadcastManager? = null

    companion object {
        private const val LOG_TAG = "CallService"
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        Log.i(LOG_TAG, "onCallAdded: $call")
        Log.i(LOG_TAG, "onCallAdded: ${call.details.handle.schemeSpecificPart}")
        broadcaster = LocalBroadcastManager.getInstance(this)
        call.registerCallback(callCallback)

        CallManager.getInstance().updateCall(call)

        val intent = Intent(this, IncomingCallActivity::class.java)
        intent.putExtra("TELNUM", call.details.handle.toString().substring(4))
        startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK).addFlags(FLAG_ACTIVITY_CLEAR_TASK).addFlags(FLAG_ACTIVITY_CLEAR_TOP))
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        Log.i(LOG_TAG, "onCallRemoved: $call")
        CallManager.getInstance().updateCall(null)
        call.unregisterCallback(callCallback)
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            Log.i(LOG_TAG, "Call.Callback onStateChanged: $call, state: $state")
            if (state == Call.STATE_DISCONNECTED) {
                val intent = Intent("finish_incoming_call_activity")
                broadcaster?.sendBroadcast(intent)
            }
        }
    }
}
