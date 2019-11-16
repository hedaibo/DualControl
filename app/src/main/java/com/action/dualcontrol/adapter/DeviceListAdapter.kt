package com.action.dualcontrol.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.action.dualcontrol.R
import com.action.dualcontrol.activity.DeviceListActivty
import com.action.dualcontrol.activity.DisplayActivty
import com.action.dualcontrol.model.DevicesInfo
import com.action.dualcontrol.utils.IpUtils
import com.action.dualcontrol.utils.LogUtils

class DeviceListAdapter:RecyclerView.Adapter<DeviceListAdapter.ListHolder> {
    private var mList = ArrayList<DevicesInfo>()
    private var mContext:Context
    constructor(context:Context){
        mContext = context
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListHolder {
        //var view = View.inflate(parent.context, R.layout.list_item,null)
        var view = LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false)
        return ListHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: ListHolder, position: Int) {
        holder.text.text = mList.get(position).name
        holder.ip.text = if(mList.get(position).ipAddress.length > 5){  mList.get(position).ipAddress }else{ mList.get(position).ip6Address}
        holder.itemView.setOnClickListener {
            LogUtils.i("clickItem:$position")

            val intent = Intent(mContext,DisplayActivty::class.java)
            val bundle = Bundle()
            bundle.putString(IpUtils.IP_FIRST,mList.get(position).ipAddress)
            bundle.putString(IpUtils.IP_SECOND,mList.get(position).ip6Address)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtras(bundle)
            mContext.startActivity(intent)

            (mContext as? DeviceListActivty)?.finish()
        }
    }

    class ListHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var ip: TextView = itemView!!.findViewById(R.id.tv_list_ip)
        var text:TextView = itemView!!.findViewById(R.id.tv_list_name)
    }


    public fun updateData(list:List<DevicesInfo>){
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }
}