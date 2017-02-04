package com.github.ajalt.flexadapter

import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * A selectable adapter item that's designed for use with the Kotlin Extensions for Android.
 *
 * @see FlexAdapterExtensionItem
 * @see FlexAdapterSelectableItem
 */
abstract class FlexAdapterSelectableExtensionItem(@LayoutRes val layoutRes: Int,
                                                  override val dragDirs: Int = 0,
                                                  override val swipeDirs: Int = 0,
                                                  override val span: Int = 1) :
        FlexAdapterSelectableItem<FlexAdapterSelectableExtensionItem.ViewHolder>() {
    abstract fun bindItemView(itemView: View, position: Int)

    override fun viewHolderFactory(): (ViewGroup) -> ViewHolder = {
        ViewHolder(LayoutInflater.from(it.context).inflate(layoutRes, it, false))
    }

    override fun bindViewHolder(holder: ViewHolder, position: Int) {
        bindItemView(holder.itemView, position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
