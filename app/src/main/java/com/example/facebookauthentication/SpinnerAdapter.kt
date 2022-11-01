package com.example.facebookauthentication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView

class SpinnerAdapter(
    private var mContext: Context?,
    private var mList: MutableList<String>?
) :
    BaseAdapter() {

    private var mInfater: LayoutInflater? = null

    init {
        spinnerAdapter(mContext!!)
    }

    fun spinnerAdapter(mContext: Context) {
        this.mContext = mContext
        mInfater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return mList!!.size
    }

    override fun getItem(position: Int): Any? {
        return mList!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var convertView: View? = convertView
        val holder: ViewHolder
        if (convertView == null) {
            holder = ViewHolder()
            convertView = mInfater?.inflate(R.layout.spinner_background, parent, false)
            holder.tvContainer = convertView?.findViewById(R.id.tv_background)
            convertView?.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        holder.tvContainer?.text = mList!![position]
        return convertView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var convertView: View? = convertView
        val holder: ViewHolder
        if (convertView == null) {
            holder = ViewHolder()
            convertView = mInfater?.inflate(R.layout.spinner_dropdown, parent, false)
            holder.tvContainer = convertView?.findViewById(R.id.tv_dropdown)
            holder.layout = convertView?.findViewById(R.id.dropdown_spinner)
            convertView?.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
//        if (position == 0)
//            holder.tvContainer?.visibility = View.GONE
//        else
//            holder.tvContainer?.visibility = View.VISIBLE
        holder.tvContainer?.text = mList!![position]
        return convertView
    }

    private class ViewHolder {
        var tvContainer: TextView? = null
        var layout: LinearLayout? = null
    }
}