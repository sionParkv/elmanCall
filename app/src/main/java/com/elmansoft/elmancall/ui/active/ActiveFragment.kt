package com.elmansoft.elmancall.ui.active

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startForegroundService
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.elmansoft.elmancall.PopupService
import com.elmansoft.elmancall.R
import com.elmansoft.elmancall.UserInfo
import com.elmansoft.elmancall.databinding.FragmentActiveBinding
import com.squareup.picasso.Picasso


class ActiveFragment : Fragment() {

    private val TAG = "PPSE"

    private var _binding: FragmentActiveBinding? = null

    private val binding get() = _binding!!

    private var isActive = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewModelProvider(this)[ActiveViewModel::class.java]

        _binding = FragmentActiveBinding.inflate(inflater, container, false)
        val root: View = binding.root

        _binding?.imageView5?.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.alpha = 0.5F
                }
                MotionEvent.ACTION_UP -> {
                    view.alpha = 1F
                    onClickImage()
                }
            }
            true
        }

        _binding?.imageView4?.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.alpha = 0.5F
                }
                MotionEvent.ACTION_UP -> {
                    view.alpha = 1F
                    onClickImage()
                }
            }
            true
        }

        //
        val info = UserInfo.getInstance().data;

        _binding?.textView22?.text = info?.spjangnm;
        _binding?.textView23?.text = info?.pernm;

        Picasso.with(context).load("https://api.elmansoft.com/etc/getCustImage.php?database=${info?.custcd}&custcd=${info?.custcd}&spjangcd=${info?.spjangcd}").into(_binding?.imageView9);

        check()

        return root
    }

    override fun onDestroyView() {
        LocalBroadcastManager.getInstance(this.requireContext()).unregisterReceiver(statusReceiver)
        super.onDestroyView()
        _binding = null
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        val filter = IntentFilter("com.elmansoft.elmancall.ui.active")
        LocalBroadcastManager.getInstance(this.requireContext()).registerReceiver(statusReceiver, filter)
        Log.d(TAG, "override fun onViewStateRestored(savedInstanceState: Bundle?)");
        super.onViewStateRestored(savedInstanceState)
    }

    private fun onClickImage() {
        if (isActive) {
            deActive()
        } else {
            active()
        }
    }

    private fun check() {
//        val roleManager: RoleManager = activity?.getSystemService(AppCompatActivity.ROLE_SERVICE) as RoleManager
//        if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
        if (!PopupService.IS_RUNNING) {
            _binding?.imageView5?.setImageDrawable(
                ResourcesCompat.getDrawable(resources, R.drawable.off, null)
            )

            _binding?.imageView4?.setImageDrawable(
                ResourcesCompat.getDrawable(resources, R.drawable.disabled, null)
            )

            _binding?.textView3?.text = "적용을 위하여"
            _binding?.textView26?.text = "활성화를 켜주세요."
            _binding?.textView2?.setTextColor(Color.parseColor("#FFFFFF"))

            isActive = false
        } else {
            _binding?.imageView5?.setImageDrawable(
                ResourcesCompat.getDrawable(resources, R.drawable.on, null)
            )

            _binding?.imageView4?.setImageDrawable(
                ResourcesCompat.getDrawable(resources, R.drawable.activate, null)
            )

            _binding?.textView3?.text = "적용중 입니다."
            _binding?.textView26?.text = "등록된 정보로 알려드립니다."
            _binding?.textView2?.setTextColor(Color.parseColor("#01418C"))

            isActive = true

            active()
        }
    }

    /*
    private fun active() {
        val manager: PackageManager? = activity?.packageManager
        val component = ComponentName(activity?.applicationContext!!, IncomingCallActivity::class.java)
        manager?.setComponentEnabledSetting(
            component,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        val roleManager: RoleManager = activity?.getSystemService(AppCompatActivity.ROLE_SERVICE) as RoleManager
        if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                intent.putExtra(
                    TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                    activity?.packageName
                )
                startActivity(intent)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                activity?.startActivityIfNeeded(intent, 25)
            }
        }
    }

    private fun deActive() {
        val manager: PackageManager? = activity?.packageManager
        val component = ComponentName(activity?.applicationContext!!, IncomingCallActivity::class.java)
        manager?.setComponentEnabledSetting(
            component,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }
    */
    private fun active() {
        startForegroundService(this.requireContext(), Intent(this.context, PopupService::class.java))
    }

    private fun deActive() {
        this.requireContext().stopService(Intent(this.context, PopupService::class.java))
    }

    override fun onResume() {
        super.onResume()
        check()
    }

    val statusReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            check()
        }
    }
}