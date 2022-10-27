package com.elmansoft.elmancall.ui.contact

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elmansoft.elmancall.UserInfo
import com.elmansoft.elmancall.databinding.FragmentContactBinding
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.change_modal.*
import kotlinx.android.synthetic.main.fragment_contact.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ContactFragment : Fragment() {

    private var _binding: FragmentContactBinding? = null

    private val binding get() = _binding!!

    private lateinit var searchView: SearchView

    lateinit var dialog: AlertDialog

    var page = 0

    var searchQuery = ""

    private var flags = arrayOf("%", "0", "1", "2", "3", "4", "5", "6")

    val contactList: ArrayList<ContactData> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentContactBinding.inflate(inflater, container, false)
        _binding?.rvContact?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        _binding?.rvContact?.setHasFixedSize(true)

        _binding?.rvContact?.adapter = ContactAdapter(contactList)
        val root: View = binding.root

        dialog = setProgressDialog(requireContext(), "검색중..")

        //
        searchView = _binding?.searchView!!
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                page = 0
                searchQuery = query ?: ""
                contactList.clear()
                requestSearch()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        _binding?.rvContact?.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!binding.rvContact.canScrollVertically(1)) {
                    requestSearch()
                }
            }
        })

        _binding?.tab?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                page = 0
                contactList.clear()
                requestSearch()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })


        return root
    }

    fun requestSearch() {
        if (page == -1) {
            return
        }

        dialog.show()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.elmansoft.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val contactService: ContactService = retrofit.create(ContactService::class.java)

        val info = UserInfo.getInstance().data

        contactService.retrieve(
            info?.custcd!!,
            flags[_binding?.tab?.selectedTabPosition!!],
            (page++).toString(),
            searchQuery,
            info.custcd
        ).enqueue(object : Callback<ContactDTO> {
            override fun onFailure(call: Call<ContactDTO>, t: Throwable){
                dialog.cancel()
                alert("오류", "서버 오류입니다.\n엘맨소프트에 문의해주세요.")
            }
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ContactDTO>, response: Response<ContactDTO>) {
                val info = response.body()
                if (info?.response?.isEmpty() == true) {
                    page = -1
                    dialog.cancel()
                    return
                }

                info?.response?.forEach {
                    contactList.add(ContactData(it.actmail, it.address, it.fax, it.email, it.tels, "상세정보보기" , it.lat, it.lng))
                }
                _binding?.rvContact?.adapter?.notifyDataSetChanged()
                dialog.cancel()
            }
        })
    }

    fun alert(title: String, msg: String) {
        val dialog = context?.let { AlertDialog.Builder(it) }
        dialog?.setTitle(title)
        dialog?.setMessage(msg)
        dialog?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setProgressDialog(context: Context, message:String): AlertDialog {
        val llPadding = 30
        val ll = LinearLayout(context)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam

        llParam = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(context)
        tvText.text = message
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 20.toFloat()
        tvText.layoutParams = llParam

        ll.addView(progressBar)
        ll.addView(tvText)

        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setView(ll)

        val dialog = builder.create()
        val window = dialog.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog.window?.attributes = layoutParams
        }
        return dialog
    }
}