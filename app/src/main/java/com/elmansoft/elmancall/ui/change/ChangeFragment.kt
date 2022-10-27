package com.elmansoft.elmancall.ui.change

import android.annotation.SuppressLint
import android.graphics.Color.*
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.elmansoft.elmancall.R
import com.elmansoft.elmancall.UserInfo
import com.elmansoft.elmancall.databinding.FragmentChangeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChangeFragment : Fragment() {

    private var _binding: FragmentChangeBinding? = null

    private val binding get() = _binding!!

    private var accounts: Array<ChangeAccountDTO>? = null

    private var popupTelnum: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangeBinding.inflate(inflater, container, false)

        retrieve()

        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun retrieve() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api2.elmansoft.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val changeAccountService: ChangeAccountService = retrofit.create(ChangeAccountService::class.java)

        changeAccountService.retrieve(UserInfo.getInstance().data?.custcd).enqueue(object : Callback<Array<ChangeAccountDTO>> {
            override fun onFailure(call: Call<Array<ChangeAccountDTO>>, t: Throwable) {
                alert("오류", "서버 오류입니다.\n엘맨소프트에 문의해주세요.")
            }

            override fun onResponse(
                call: Call<Array<ChangeAccountDTO>>,
                response: Response<Array<ChangeAccountDTO>>
            ) {
                val info = response.body()
                if (info?.isEmpty() == true) {
                    return
                }

                accounts = info
                print()
            }
        })
    }

    @SuppressLint("InflateParams")
    fun print() {
        var i = 1
        _binding?.accountContainer?.removeAllViews()
        accounts?.forEach { it ->
            val num = it
            val layoutTel = layoutInflater.inflate(R.layout.change_item, null)
            val circle = layoutTel.findViewById<TextView>(R.id.textView21)
            val title = layoutTel.findViewById<TextView>(R.id.textView20)
            val number = layoutTel.findViewById<TextView>(R.id.textView27)
            val target = layoutTel.findViewById<TextView>(R.id.textView28)

            circle.text = i++.toString()
            number.text = num.telnum

            if (num.telstate == 0) {
                title.text = "   정상통화"
            } else {
                title.text = "   착신전환"
                circle.setBackgroundResource(R.drawable.change_circle_active)
                title.setTextColor(ContextCompat.getColor(requireContext(), R.color.pink))
                number.paintFlags = number.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                target.text = "%s %s".format(num.recvtel, num.recvpernm)
                target.visibility = View.VISIBLE
            }

            layoutTel.setOnClickListener {
                popupTelnum = num.telnum

                if (num.telstate == 0) {
                    val dialog = ChangeTargetDialog(context as AppCompatActivity)
                    dialog.setOnResultListener { tel -> requestChange(tel, num.telstate == 0) }
                    dialog.show()
                } else {
                    requestChange("01000000001", false)
                }
            }
            _binding?.accountContainer?.addView(layoutTel)
        }
    }

    private fun requestChange(handphone: String, isApply: Boolean) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.elmansoft.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val changeAccountService: ChangeAccountService = retrofit.create(ChangeAccountService::class.java)
        val user = UserInfo.getInstance().data

        Thread{
            changeAccountService.change(
                user?.custcd!!,
                user.custcd,
                "1",
                if (isApply) "1" else "0",
                popupTelnum,
                handphone,
            ).execute()
        }.start()

        Handler().postDelayed({
            Thread{
                changeAccountService.change(
                    user?.custcd!!,
                    user.custcd,
                    "5",
                    "",
                    "",
                    "",
                ).execute()
            }.start()
        }, 1000)

        Handler().postDelayed({
            retrieve()
        }, 5000)

        Toast.makeText(context, "5초 뒤 새로고침 됩니다", Toast.LENGTH_LONG).show()
    }

    fun alert(title: String, msg: String) {
        var dialog = context?.let { AlertDialog.Builder(it) }
        dialog?.setTitle(title)
        dialog?.setMessage(msg)
        dialog?.show()
    }
}