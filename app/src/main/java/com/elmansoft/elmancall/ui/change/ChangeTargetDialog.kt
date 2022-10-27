package com.elmansoft.elmancall.ui.change

import android.app.Dialog
import android.graphics.Paint
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.elmansoft.elmancall.R
import com.elmansoft.elmancall.UserInfo
import com.elmansoft.elmancall.databinding.ChangeModalBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChangeTargetDialog(private val context: AppCompatActivity) {

    private lateinit var binding: ChangeModalBinding
    private val dlg = Dialog(context)   //부모 액티비티의 context 가 들어감
    private var users: Array<ChangeUserDTO>? = null
    private var target: String = ""

    private lateinit var listener: OnResultListener

    fun show() {
        binding = ChangeModalBinding.inflate(context.layoutInflater)

        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        dlg.setContentView(binding.root)     //다이얼로그에 사용할 xml 파일을 불러옴
        dlg.setCancelable(false)    //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함

        search()

        binding.search.setOnClickListener { search() }
        binding.searchQuery.isIconified = false
        binding.searchQuery.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                search()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        binding.confirm.setOnClickListener {
            dlg.dismiss()
            listener.onResult(target)
        }

        binding.close.setOnClickListener {
            dlg.dismiss()
        }

        dlg.show()
    }

    fun search() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.elmansoft.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val changeAccountService: ChangeAccountService = retrofit.create(ChangeAccountService::class.java)
        val user = UserInfo.getInstance().data

        changeAccountService.users(
            user?.custcd!!,
            user.spjangcd,
            user.custcd,
            "0",
            binding.searchQuery.query.toString(),
        ).enqueue(object :
            Callback<Array<ChangeUserDTO>> {
            override fun onFailure(call: Call<Array<ChangeUserDTO>>, t: Throwable) {
                alert("오류", "서버 오류입니다.\n엘맨소프트에 문의해주세요.")
            }

            override fun onResponse(
                call: Call<Array<ChangeUserDTO>>,
                response: Response<Array<ChangeUserDTO>>
            ) {
                val data = response.body()
                if (data?.isEmpty() == true) {
                    return
                }

                users = data
                print()
            }
        })
    }

    fun print() {
        binding.container.removeAllViews()
        users?.forEach {
            val item = it
            val layoutTel = context.layoutInflater.inflate(R.layout.change_modal_item, null)
            val radio = layoutTel.findViewById<ImageView>(R.id.radio)
            val name = layoutTel.findViewById<TextView>(R.id.name)

            item.chk = false
            radio.setImageResource(R.drawable.circle_de)

            name.text = item.pernm

            layoutTel.setOnClickListener {
                item.chk = true
                target = item.handphone
                if (item.chk) {
                    radio.setImageResource(R.drawable.circle)
                }
            }

            binding.container.addView(layoutTel)
        }
    }

    fun alert(title: String, msg: String) {
        var dialog = context?.let { AlertDialog.Builder(it) }
        dialog?.setTitle(title)
        dialog?.setMessage(msg)
        dialog?.show()
    }

    fun setOnResultListener(listener: (String) -> Unit) {
        this.listener = object: OnResultListener {
            override fun onResult(content: String) {
                listener(content)
            }
        }
    }

    interface OnResultListener {
        fun onResult(content: String)
    }
}
