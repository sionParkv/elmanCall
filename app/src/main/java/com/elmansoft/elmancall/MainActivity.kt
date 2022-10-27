package com.elmansoft.elmancall

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.KeyguardManager
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telecom.TelecomManager
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.elmansoft.elmancall.databinding.ActivityMainBinding
import com.elmansoft.elmancall.databinding.ActivityPopupCallBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val roleManager by lazy { getSystemService(RoleManager::class.java) }

    @SuppressLint("RestrictedApi", "InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_active,
                R.id.navigation_contact,
                R.id.navigation_change,
                R.id.navigation_list,
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        lateinit var binding : ActivityPopupCallBinding
        val dlg = Dialog(this)

        val actnm = intent.getStringExtra("actnm")
        val tels = intent.getStringArrayListExtra("tels")

//        requestRole()

        if (checkDrawOverlayPermission()) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
            startForegroundService(Intent(this, PopupService::class.java))
        }
    }

    private fun requestRole() {
        val roleManager: RoleManager = getSystemService(ROLE_SERVICE) as RoleManager
//        dialogResize(this,0.8f,0.4f)

        if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
            var intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.activity_popup_call, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
            intent.putExtra(
                TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                this.packageName
            )
            startActivity(intent)
            mBuilder.show()

            intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
            startActivityIfNeeded(intent, 25)
        }
    }

    private fun checkDrawOverlayPermission(): Boolean {
        if (Settings.canDrawOverlays(this)) {
            return true
        }

        this.runOnUiThread {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)

            val result = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (Settings.canDrawOverlays(this)) {
                    startService(Intent(this, PopupService::class.java))
                } else {
                    Toast.makeText(this, "화면 오버레이 기능이 비활성화 되어 발신자 번호 표시 서비스를 이용하실 수 없습니다", Toast.LENGTH_SHORT).show()
                }
            }

            Toast.makeText(this, "화면 오버레이 기능을 활성화 해주세요", Toast.LENGTH_SHORT).show()
            result.launch(intent)
        }

        return false
    }
//    private fun Context.dialogResize(dialog: MainActivity, width: Float, height: Float){
//        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
//
//        if (Build.VERSION.SDK_INT < 30){
//            val display = windowManager.defaultDisplay
//            val size = Point()
//
//            display.getSize(size)
//
//            val window = dialog.window
//
//            val x = (size.x * width).toInt()
//            val y = (size.y * height).toInt()
//
//            window?.setLayout(x, y)
//
//        }else{
//            val rect = windowManager.currentWindowMetrics.bounds
//
//            val window = dialog.window
//            val x = (rect.width() * width).toInt()
//            val y = (rect.height() * height).toInt()
//
//            window?.setLayout(x, y)
//        }
//    }
}