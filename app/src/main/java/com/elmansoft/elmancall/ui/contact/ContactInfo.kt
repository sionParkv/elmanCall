package com.elmansoft.elmancall.ui.contact

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.elmansoft.elmancall.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_contact_info.*

class ContactInfo : AppCompatActivity() {
    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_info)

        val actnm = intent.getStringExtra("actnm")
        val address = intent.getStringExtra("address")
        val lat = intent.getStringExtra("lat")
        val lng = intent.getStringExtra("lng")
        val tels = intent.getStringArrayListExtra("tels")

        Picasso.with(this)
            .load("https://api.elmansoft.com/gps/naver_static.php?w=600&h=300&scale=2&center=${lng},${lat}&markers=type:d|size:small|pos:${lng}%20${lat}&level=16")
            .into(mapView)

        textView13.text = actnm
        textView14.text = address

        tels?.forEach {
            val num = it
            val layoutTel = layoutInflater.inflate(R.layout.telnum_layout, null)
            layoutTel.findViewById<TextView>(R.id.number).text = num
            layoutTel.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:${num.replace("-", "")}")
                startActivity(intent)
            }
            telContents.addView(layoutTel)
        }

        top.setOnClickListener { finish() }
    }
}