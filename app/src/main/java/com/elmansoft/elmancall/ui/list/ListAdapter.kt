package com.elmansoft.elmancall.ui.list

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elmansoft.elmancall.R

class ListAdapter(val ListList: ArrayList<ListData>) : RecyclerView.Adapter<ListAdapter.CustomViewHolder> (){


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListAdapter.CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_recycler, parent,false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListAdapter.CustomViewHolder, position: Int) {
        holder.user.setImageResource(ListList.get(position).user)
        holder.actnm.text = ListList.get(position).actnm
        holder.telnm.text = ListList.get(position).telnm
        holder.caltm.text = ListList.get(position).caltm
        holder.type.text = ListList.get(position).type
    }

    override fun getItemCount(): Int {
        return ListList.size
    }

    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val user = itemView.findViewById<ImageView>(R.id.imageView6)
        val actnm = itemView.findViewById<TextView>(R.id.textView10)
        val telnm = itemView.findViewById<TextView>(R.id.textView11)
        val caltm = itemView.findViewById<TextView>(R.id.textView12)
        val type = itemView.findViewById<TextView>(R.id.textView15)
    }
}