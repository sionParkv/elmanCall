package com.elmansoft.elmancall

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.URL


class PopupService() : Service() {
    private val TAG = "PPSE"
    private var CurrentState = -1
    private var mManager: WindowManager? = null
    private var mParams: WindowManager.LayoutParams? = null
    private var titleContainer: LinearLayout? = null
    private var titleView: TextView? = null
    private var detailView: TextView? = null
    private var myPhoneNumber: String? = null
    private var notification: Notification? = null

    companion object {
        @JvmStatic var IS_RUNNING = false
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "override fun onCreate()")

        myPhoneNumber = getPhoneNumber()

        val channel = NotificationChannel(TAG, "엘맨 발신자 번호표시", NotificationManager.IMPORTANCE_DEFAULT)

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)

        val intented = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intented,
            PendingIntent.FLAG_IMMUTABLE
        )

        notification = NotificationCompat.Builder(this, TAG)
            .setContentText("")
            .setContentText("")
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)


        val mInflater = LayoutInflater.from(this).inflate(R.layout.activity_popup_call, null, false)
        titleContainer = mInflater.findViewById<View>(R.id.activity_popup_call) as LinearLayout
        titleView = mInflater.findViewById<View>(R.id.textView33) as TextView
        detailView = mInflater.findViewById<View>(R.id.textView34) as TextView

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            mParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            )
        } else {
            mParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH),
                PixelFormat.TRANSLUCENT
            )
        }

        mManager = getSystemService(WINDOW_SERVICE) as WindowManager
        mManager!!.addView(titleContainer, mParams)

        mManager!!.updateViewLayout(titleContainer, mParams)

        titleContainer!!.visibility = View.GONE

        val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        registerReceiver(receiver, filter)
        IS_RUNNING = true

        val intent = Intent("com.elmansoft.elmancall.ui.active")
        intent.putExtra("STATUS", true)
        LocalBroadcastManager.getInstance(this)
            .sendBroadcast(intent)
        Log.d(TAG, "sendBroadcast(intent)");
    }

    override fun onDestroy() {
        Log.d(TAG, "override fun onDestroy()");
        unregisterReceiver(receiver)
        stopForeground(true)
        stopSelf()
        IS_RUNNING = false

        val intent = Intent("com.elmansoft.elmancall.ui.active")
        intent.putExtra("STATUS", false)
        LocalBroadcastManager.getInstance(this)
            .sendBroadcast(intent)

        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int");
        return super.onStartCommand(intent, flags, startId)
    }

    fun showCID(number: String?) {
        detailView!!.text = number
        titleContainer!!.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.Default).launch {
            val cid = getCID(number)
            if (cid == null) {
                titleView!!.text = "알수없음"
            } else {
                val spCid = cid.split("|")
                titleView!!.text = spCid[0]
                detailView!!.text = spCid[1]
            }
        }
    }

    fun hideCID() {
        titleContainer!!.visibility = View.GONE
    }

    val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            Log.d(TAG, "override fun onReceive(p0: Context?, p1: Intent?)");
            val extras = p1?.extras
            val state = extras?.getString(TelephonyManager.EXTRA_STATE)
            val number = extras?.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)

            if (TextUtils.isEmpty(number)) return

            when (state) {
                "IDLE" -> {
                    CurrentState = TelephonyManager.CALL_STATE_IDLE
                    hideCID()
                }
                "OFFHOOK" -> {
                    CurrentState = TelephonyManager.CALL_STATE_OFFHOOK
                    hideCID()
                }
                "RINGING" -> {
                    CurrentState = TelephonyManager.CALL_STATE_RINGING
                    showCID(number)
                }
            }
        }
    }

    private fun getCID(target: String?): String? {
        val url = URL("https://api.elmansoft.com/subreq/findWhois.php")
        val postData = "tel=${myPhoneNumber}&target=${target}"

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

        if (response == "NULL") return null
        return response
    }

    @SuppressLint("MissingPermission")
    fun getPhoneNumber(): String {
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.line1Number
    }
}