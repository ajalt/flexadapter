package com.github.ajalt.flexadapter

import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * An adapter item that's designed for use with the Kotlin Extensions for Android.
 *
 * Definition and creation of the view holder is taken care of, so subclasses just need define a
 * binder function.
 */
abstract class FlexAdapterExtensionItem(@LayoutRes val layoutRes: Int) : FlexAdapterItem<FlexAdapterExtensionItem.ViewHolder>() {
    abstract fun bindItemView(itemView: View, position: Int)

    override fun createViewHolder(): (ViewGroup) -> ViewHolder = {
        ViewHolder(LayoutInflater.from(it.context).inflate(layoutRes, it, false))
    }

    override fun bindViewHolder(holder: ViewHolder, position: Int) {
        bindItemView(holder.itemView, position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
