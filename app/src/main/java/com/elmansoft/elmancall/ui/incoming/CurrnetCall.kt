package com.elmansoft.elmancall.ui.incoming

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.elmansoft.elmancall.CallManager
import com.elmansoft.elmancall.MainActivity
import com.elmansoft.elmancall.R
import kotlinx.android.synthetic.main.activity_currnet_call.*
import java.util.*
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration


class CurrnetCall : AppCompatActivity() {

    private var isSpeaker = false
    private lateinit var am: AudioManager
    private lateinit var timer: ScheduledFuture<*>;
    private val initTime = System.currentTimeMillis()

    @SuppressLint("ResourceType", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currnet_call)

        am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.mode = AudioManager.MODE_IN_CALL;

        isSpeaker = am.isSpeakerphoneOn;

        val intent = Intent(this, MainActivity::class.java)
        val executor = ScheduledThreadPoolExecutor(15)
        timer = executor.scheduleAtFixedRate(object: TimerTask() {
            override fun run() {
                val times = System.currentTimeMillis() - initTime
                val duration = times.toDuration(DurationUnit.MILLISECONDS)
                val mins = "00${duration.inWholeMinutes}"
                val seconds = "00${duration.inWholeSeconds % 60}"
                textView25.text = String.format("%s:%s", mins.substring(mins.length - 2), seconds.substring(seconds.length - 2))
            }
        }, 0, 1, TimeUnit.SECONDS)

        if(CallManager.getInstance().call?.details?.state == Call.STATE_DISCONNECTED){
            CallManager.getInstance().call?.disconnect()
            startActivity(intent)
        }

        Log.d("d",CallManager.getInstance().call?.details.toString())


        imageView12.setOnClickListener {
            if (CallManager.getInstance().call?.details?.state != Call.STATE_DISCONNECTED) {
                CallManager.getInstance().call?.disconnect()
            } else {
                CallManager.getInstance().call?.reject(Call.REJECT_REASON_DECLINED)
            }
            startActivity(intent)
        }

        imageView11?.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.alpha = 0.5F
                }
                MotionEvent.ACTION_UP -> {
                    view.alpha = 1F
                    am.isSpeakerphoneOn
                }
            }
            true
        }

        fun volChange(volume: Int){
            val audioManager = this.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            when(volume){
                -1 -> audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                else -> {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_RING,
                        (audioManager.getStreamMaxVolume(AudioManager.STREAM_RING) * volume/100.0).toInt(),
                        AudioManager.FLAG_PLAY_SOUND
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        timer.cancel(false);
    }

    private val br: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver((br),
            IntentFilter("finish_incoming_call_activity")
        )
    }
}