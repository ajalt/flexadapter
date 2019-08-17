package com.github.ajalt.flexadapter

import android.support.annotation.LayoutRes
import android.view.LayoutInflater
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
 * @param dragDirs Equivalent to overriding [AdapterItem.dragDirs]
 * @param swipeDirs Equivalent to overriding [AdapterItem.swipeDirs]
 * @param span Equivalent to overriding [AdapterItem.span]
 */
abstract class CachingAdapterItem(
        @LayoutRes val layoutRes: Int,
        override val dragDirs: Int = 0,
        override val swipeDirs: Int = 0,
        override val span: Int = 1
) : AdapterItem<CachingViewHolder>() {
    /**
     * Bind this item's data to its inflated view.
     *
     * @param position The index of this item in [FlexAdapter.items]
     */
    abstract fun CachingViewHolder.bindItemView(position: Int)

    /**
     * Optional method called once after the view holder is created to perform one-time view setup.
     */
    open fun CachingViewHolder.initializeItemView() {}

    final override fun viewHolderFactory(): (ViewGroup) -> CachingViewHolder = {
        CachingViewHolder(LayoutInflater.from(it.context).inflate(layoutRes, it, false)).apply {
            initializeItemView()
        }
    }

    final override fun bindViewHolder(holder: CachingViewHolder, position: Int) {
        holder.bindItemView(position)
    }
}
