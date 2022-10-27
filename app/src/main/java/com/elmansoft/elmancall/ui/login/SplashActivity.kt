package com.elmansoft.elmancall.ui.login

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.elmansoft.elmancall.R
import com.google.gson.JsonParser
import com.markodevcic.peko.ActivityRotatingException
import com.markodevcic.peko.Peko
import com.markodevcic.peko.PermissionResult
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.URL
import kotlin.coroutines.CoroutineContext

class SplashActivity : AppCompatActivity(), CoroutineScope {

    private var job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (Peko.isRequestInProgress()) {
            launch {
                setResults(Peko.resumeRequest())
            }
        }

        checkUpdate()
    }

    companion object {
        private const val DURATION : Long = 1500
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun checkUpdate() {
        val self = this
        CoroutineScope(Dispatchers.Default).launch {
            val version = packageManager.getPackageInfo(packageName, 0).longVersionCode
            val newVersion = JsonParser().parse(getVersion()).asJsonObject.get("version").asLong

            self.runOnUiThread(java.lang.Runnable {
                if (version != newVersion) {
                    val dialog = AlertDialog.Builder(self)
                    dialog.setTitle("새 버전(v${newVersion})")
                    dialog.setMessage("새로운 버전이 있어서 업데이트가 필요합니다.\n현재 v${version}")
                    dialog.setOnDismissListener {
                        val url = "https://api.elmansoft.com/elman_call/app-release.apk"
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        startActivity(intent)
                        finish()
                    }
                    dialog.show()
                } else {
                    request()
                }
            })
        }
    }

    private fun setResults(result: PermissionResult) {
        if (result is PermissionResult.Granted) {
            Handler().postDelayed({
                val intent = Intent(this, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                finish()
            }, DURATION)
        } else if (result is PermissionResult.Denied) {
            Toast.makeText(applicationContext, "권한이 부여되지 않아 앱을 종료합니다", Toast.LENGTH_LONG).show()
            finish()
//            result.deniedPermissions.forEach { p ->
//                // this one was denied
//            }
        }
    }

    private fun request() {
        launch {
            val result = Peko.requestPermissionsAsync(
                applicationContext,
                Manifest.permission.MANAGE_OWN_CALLS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.DISABLE_KEYGUARD,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ANSWER_PHONE_CALLS,
                Manifest.permission.READ_SMS,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_PHONE_NUMBERS,
            )
            setResults(result)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isChangingConfigurations) {
            job.completeExceptionally(ActivityRotatingException())
        } else {
            job.cancel()
        }
    }

    private fun getVersion(): String? {
        val url = URL("https://api.elmansoft.com/elman_call/version.php")
        val postData = ""

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
}