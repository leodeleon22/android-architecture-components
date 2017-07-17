package com.example.android.contentprovidersample.view

import android.database.Cursor
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.example.android.contentprovidersample.data.Cheese

/**
 * Created by leo on 17/07/2017.
 */
class CheeseAdapter : RecyclerView.Adapter<CheeseAdapter.ViewHolder>() {

    private var mCursor: Cursor? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (mCursor!!.moveToPosition(position)) {
            holder.mText.text = mCursor!!.getString(
                    mCursor!!.getColumnIndexOrThrow(Cheese.COLUMN_NAME))
        }
    }

    override fun getItemCount(): Int {
        return if (mCursor == null) 0 else mCursor!!.count
    }

    internal fun setCheeses(cursor: Cursor?) {
        mCursor = cursor
        notifyDataSetChanged()
    }

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(
            android.R.layout.simple_list_item_1, parent, false)) {

        var mText: TextView

        init {
            mText = itemView.findViewById(android.R.id.text1) as TextView
        }

    }

}