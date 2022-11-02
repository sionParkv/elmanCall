package com.elmansoft.elmancall.ui.list

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.CallLog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleCursorAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.database.getStringOrNull
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.elmansoft.elmancall.R
import com.elmansoft.elmancall.databinding.FragmentListBinding
import java.text.ParseException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null

    private val binding get() = _binding!!

    @SuppressLint("Recycle")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val formatterYearMonDate = DateTimeFormatter.ofPattern("yyyy.MM.dd") // HH시 mm분 ss초
        val formatterHourMin = DateTimeFormatter.ofPattern("HH:mm") // HH시 mm분 ss초

        if (context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    android.Manifest.permission.READ_CALL_LOG
                )
            } != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(android.Manifest.permission.READ_CALL_LOG),
                111
            )
        } else {
            readLog()
        }

        val listList: ArrayList<ListData> = ArrayList()

        var status =
            ContextCompat.checkSelfPermission(requireContext(), "android.permission.READ_CONTACTS")
        if (status == PackageManager.PERMISSION_GRANTED) {
            Log.d("test", "permission")
        } else {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf<String>("android.permission.READ_CONTACTS"),
                100
            )
        }

        status =
            ContextCompat.checkSelfPermission(requireContext(), "android.permission.READ_CONTACTS")
        if (status == PackageManager.PERMISSION_GRANTED) {
            val resolver: ContentResolver? = context?.contentResolver
            val callLogUri = CallLog.Calls.CONTENT_URI
            val cursor: Cursor?

            try {
                cursor = resolver?.query(callLogUri, null, null, null, null)
                if (cursor != null) {
                    var maxcount = 999
                    while (cursor.moveToNext()) {
                        if (--maxcount < 0) {
                            break
                        }
//                        Log.i("Test_Log","cursor count == $maxcount =================================================")

                        var name: String = ""
                        var telnum = ""
                        var date = 0L
                        var sDate = ""
                        var type = ""

                        for (i in 0 until cursor.columnCount) {
                            val columnName = cursor.getColumnName(i)
                            val cursorIndex = cursor.getColumnIndex(columnName)

                            if (columnName.equals("name")) {
                                name = cursor.getStringOrNull(cursorIndex) ?: ""
                            } else if (columnName.equals("number")) {
                                telnum = cursor.getString(cursorIndex).convertNumberToPhoneNumber()
                            } else if (columnName.equals("date")) {
                                date = cursor.getLong(cursorIndex)
                            } else if (columnName.equals("type")) {
                                if (cursor.getString(cursorIndex).equals("3")) {
                                    type = "부재중"
                                }
                            }
                            Log.i("Test_Log","cursor - name == $columnName // ci = ${cursorIndex} // data = ${cursor.getString(cursorIndex)}")
                        }

                        val dt =
                            Date(date).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        val now = LocalDateTime.now()
                        sDate = if (now.format(formatterYearMonDate)
                                .equals(dt.format(formatterYearMonDate))
                        ) {
                            dt.format(formatterHourMin)
                        } else {
                            dt.format(formatterYearMonDate)
                        }

                        listList.add(ListData(R.drawable.uesr_b, name, telnum, sDate, type))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(context, "권한이 없어서 통화 이력을 불러오지 못했습니다", Toast.LENGTH_LONG).show()
        }

        _binding = FragmentListBinding.inflate(inflater, container, false)
        _binding?.rvList?.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        _binding?.rvList?.setHasFixedSize(true)

        _binding?.rvList?.adapter = ListAdapter(listList)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun readLog() {
        val cols: Array<String> = arrayOf(CallLog.Calls._ID, CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DURATION, CallLog.Calls.DATE)
        val rs : Cursor? = context?.contentResolver?.query(CallLog.Calls.CONTENT_URI,cols,null,null,"${CallLog.Calls.LAST_MODIFIED} DESC")
        val from: Array<String> = arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DURATION)
        var adapter = SimpleCursorAdapter(context,android.R.layout.simple_expandable_list_item_2,rs,from,
        intArrayOf(R.id.imageView6,R.id.textView10, R.id.textView11, R.id.textView12),0)
//        _binding?.rvList?.adapter = adapter

    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

        }
    }

    private fun String.convertNumberToPhoneNumber(): String {
        return try {
            val regexString = "(\\d{3})(\\d{3,4})(\\d{4})"
            return if (!Pattern.matches(regexString, this)) this else Regex(regexString).replace(
                this,
                "$1-$2-$3"
            )
        } catch (e: ParseException) {
            e.printStackTrace()
            this
        }
    }
}