package com.elmansoft.elmancall.ui.incoming

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telecom.Call
import android.telecom.VideoProfile
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.elmansoft.elmancall.CallManager
import java.net.URL
import com.elmansoft.elmancall.UserInfo
import com.elmansoft.elmancall.databinding.ActivityIncomingCallBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

class IncomingCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIncomingCallBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomingCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val targetTelnum = intent.getStringExtra("TELNUM")

        if (targetTelnum == null) {
            // Error
            finish()
            return
        }

        binding.telnum.text = targetTelnum

        CoroutineScope(Dispatchers.Default).launch {
            val cid = getCID(targetTelnum)
            if (cid == null) {
                binding.cid.text = "알수없음"
            } else {
                val spCid = cid.split("|")
                binding.cid.text = spCid[0]
            }
        }
    }

    private val br: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver((br),
            IntentFilter("finish_incoming_call_activity"))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(br)
    }

    private fun getCID(target: String): String? {
        val user = UserInfo.getInstance()

        val url = URL("https://api.elmansoft.com/subreq/findWhois.php")
        val postData = "tel=${user.telnum}&target=${target}"

        val conn = url.openConnection()
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        conn.setRequestProperty("Content-Length", postData.length.toString())


        var response = ""
        var line: String?
        DataOutputStream(conn.getOutputStream()).use { it.writeBytes(postData) }
        BufferedReader(InputStreamReader(conn.getInputStream())).use { bf ->
            while (bf.readLine().also { line = it } != null) {
                if (line != null) {
                    response += line
                }
            }
        }

        if (response.equals("NULL")) return null
        return response
    }
    //전화 수락
    fun onClickAcceptButton(v: View) {
        CallManager.getInstance().call?.answer(VideoProfile.STATE_AUDIO_ONLY)
        val intent = Intent(this, CurrnetCall::class.java)
        startActivity(intent)
        finish()
    }
    //전화 거절
    fun onClickRefuseButton(v: View) {
        if (CallManager.getInstance().call?.details?.state != Call.STATE_DISCONNECTED) {
            CallManager.getInstance().call?.disconnect()
        } else {
            CallManager.getInstance().call?.reject(Call.REJECT_REASON_DECLINED)
        }
        finish()
    }
}