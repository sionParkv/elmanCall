package com.elmansoft.elmancall.ui.contact

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.elmansoft.elmancall.R
import kotlinx.coroutines.currentCoroutineContext

class ContactAdapter(private val data: ArrayList<ContactData>) :
    RecyclerView.Adapter<ContactAdapter.ViewHolder>() {

    private var datas = data

    lateinit var parent: ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        this.parent = parent
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_ex,parent,false)
        return ViewHolder(view)
    }
    override fun getItemCount(): Int = datas.size

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val actnm: TextView = itemView.findViewById(R.id.textView4)
        private val actadd: TextView = itemView.findViewById(R.id.textView5)
        private val fax: TextView = itemView.findViewById(R.id.textView6)
        private val mail: TextView = itemView.findViewById(R.id.textView7)
        private val telnum: TextView = itemView.findViewById(R.id.textView8)
        private val det: TextView = itemView.findViewById(R.id.textView9)

        fun bind(item: ContactData) {
            var tel = ""
            var isTowMore = false

            if (item.telnum.isNotEmpty()) {
                tel = "전화번호 - %s".format(item.telnum[0].stel)

                if (item.telnum.size > 1) {
                    tel = "전화번호 %d건".format(item.telnum.size)
                    isTowMore = true
                }
            }

            actnm.text = item.actnm
            actadd.text = "주소 - %s".format(item.actadd)
            fax.text = "팩스 - %s".format(item.fax)
            mail.text = "이메일 - %s".format(item.mail)
            telnum.text = tel
            det.text = item.det

            if (!isTowMore) {
                det.visibility = View.GONE
                val container = itemView.findViewById<LinearLayoutCompat>(R.id.container)
                container.setOnClickListener {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:${tel.replace("-", "")}")
                    parent.context.startActivity(intent)
                }
            } else {
                val telnums: ArrayList<String> = ArrayList()
                item.telnum.forEach { telnums.add(it.stel) }

                det.setOnClickListener {
                    val intent = Intent(parent.context, ContactInfo::class.java)
                    intent.putExtra("actnm", item.actnm)
                    intent.putExtra("address", item.actadd)
                    intent.putExtra("lat", item.lat)
                    intent.putExtra("lng", item.lng)
                    intent.putExtra("tels", telnums)
                    parent.context.startActivity(intent)
                }
            }
        }
    }

}