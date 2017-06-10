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
 *
 * @constructor You can pass values to the constructor instead of overriding [dragDirs],
 * [swipeDirs], and [span] manually.
 *
 * @param layoutRes The layout resource for items of this type.
 * @param dragDirs Equivalent to overriding [FlexAdapterItem.dragDirs]
 * @param swipeDirs Equivalent to overriding [FlexAdapterItem.swipeDirs]
 * @param span Equivalent to overriding [FlexAdapterItem.span]
 */
abstract class FlexAdapterExtensionItem(@LayoutRes val layoutRes: Int,
                                        override val dragDirs: Int = 0,
                                        override val swipeDirs: Int = 0,
                                        override val span: Int = 1) :
        FlexAdapterItem<FlexAdapterExtensionItem.ViewHolder>() {
    /**
     * Bind this item's data to its inflated view.
     *
     * @param itemView The view inflated from the [viewHolderFactory].
     * @param position The index of this item in [FlexAdapter.items]
     */
    abstract fun bindItemView(itemView: View, position: Int)

    override fun viewHolderFactory(): (ViewGroup) -> ViewHolder = {
        ViewHolder(LayoutInflater.from(it.context).inflate(layoutRes, it, false))
    }

    override final fun bindViewHolder(holder: ViewHolder, position: Int) {
        bindItemView(holder.itemView, position)
    }

    /** A ViewHolder that relies on kotlin android extensions for caching. */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
