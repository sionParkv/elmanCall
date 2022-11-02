package com.elmansoft.elmancall.ui.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.elmansoft.elmancall.MainActivity
import com.elmansoft.elmancall.R
import com.elmansoft.elmancall.UserInfo
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


class LoginActivity : AppCompatActivity() {
    var PREFS_NAME = "ELMAN_CALL_SETTINGS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.elmansoft.com/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
        val loginService: LoginService = retrofit.create(LoginService::class.java)

        val number = getPhoneNumber()
//        Toast.makeText(applicationContext, number, Toast.LENGTH_LONG).show();

        val settings = applicationContext.getSharedPreferences(PREFS_NAME, 0)
        val userID = settings.getString("userid", "")
        val userPW = settings.getString("userpw", "")
        editTextTextID.setText(userID)
        editTextTextPassword.setText(userPW)

        button.setOnClickListener {
            val text2 = editTextTextPassword.text.toString()
            val text1 = editTextTextID.text.toString()

            loginService.requestLogin(number, text1, text2).enqueue(object : Callback<String>{
                override fun onFailure(call: Call<String>, t: Throwable){
                    val dialog = AlertDialog.Builder(this@LoginActivity)
                    dialog.setTitle("에러")
                    dialog.setMessage("호출 실패")
                    dialog.show()
                }
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    val sp = response.body()?.split(":");
                    if (sp == null || sp.isEmpty()) {
                        alert("오류", "서버 오류입니다.\n엘맨소프트에 문의하세요.")
                        return
                    }

                    val status = sp[0]
                    val perid = sp[1]

                    if (status == "Deny" && perid.startsWith("#")) {
                        alert("오류", perid.substring(1))
                        return
                    } else if (status == "Deny") {
                        val warning = sp[2]
                        alert("오류", "비밀번호 ${warning}회 오류입니다.")
                        return
                    }

                    requestUserInfo(number, sp[2], sp[1], sp[3]);
                }
            })
        }

        if (number.isNotEmpty()) {
            requestAutoUserInfo(number)
        } else if (userID?.isNotEmpty() == true && userPW?.isNotEmpty() == true) {
            button.performClick()
        }
    }

    fun requestUserInfo(tel: String, custcd: String, perid: String, rnum: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.elmansoft.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val userInfoService: UserInfoService = retrofit.create(UserInfoService::class.java)
        val self = this

        userInfoService.request(tel, custcd, perid, rnum).enqueue(object : Callback<UserInfoDTO>{
            override fun onFailure(call: Call<UserInfoDTO>, t: Throwable){
                val dialog = AlertDialog.Builder(this@LoginActivity)
                dialog.setTitle("에러")
                dialog.setMessage("호출 실패")
                dialog.show()
            }
            override fun onResponse(call: Call<UserInfoDTO>, response: Response<UserInfoDTO>) {
                val info = response.body()
                Log.d("test",info.toString())
                UserInfo.getInstance().init(info, tel)

                val userID = editTextTextID.text.toString()
                val userPW = editTextTextPassword.text.toString()

                val settings = applicationContext.getSharedPreferences(PREFS_NAME, 0)
                val editor = settings.edit()
                editor.putString("userid", userID)
                editor.putString("userpw", userPW)
                editor.apply()

                startActivity(Intent(self, MainActivity::class.java))
                finish()
            }
        })
    }

    fun requestAutoUserInfo(tel: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.elmansoft.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val userAutoLoginService: UserAutoLoginService = retrofit.create(UserAutoLoginService::class.java)
        val self = this

        userAutoLoginService.request(tel).enqueue(object : Callback<UserInfoDTO>{
            override fun onFailure(call: Call<UserInfoDTO>, t: Throwable){
                //
            }
            override fun onResponse(call: Call<UserInfoDTO>, response: Response<UserInfoDTO>) {
                val info = response.body()
                Log.d("test",info.toString())
                UserInfo.getInstance().init(info, tel)

                startActivity(Intent(self, MainActivity::class.java))
                finish()
            }
        })
    }

    fun alert(title: String, msg: String) {
        var dialog = AlertDialog.Builder(this@LoginActivity)
        dialog.setTitle(title)
        dialog.setMessage(msg)
        dialog.show()
    }


    @SuppressLint("MissingPermission")
    fun getPhoneNumber(): String {
        var tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.line1Number
    }
}