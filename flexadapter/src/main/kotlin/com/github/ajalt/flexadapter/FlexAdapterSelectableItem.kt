package com.github.ajalt.flexadapter

import android.support.v7.widget.RecyclerView

/**
 * An item that has a selection state.
 *
 * The [FlexAdapter] will keep track of items of this type, which you can access from functions like
 * [FlexAdapter.selectedItems] and [FlexAdapter.selectAllItems].
 *
 * Note that if you want the item to update its view, you will still need to call
 * [FlexAdapter.notifyItemChanged] or similar as usual.
 */
abstract class FlexAdapterSelectableItem<VH : RecyclerView.ViewHolder> : FlexAdapterItem<VH>() {
    var selected: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            selectionChangedListener?.invoke(this)
        }

    internal var selectionChangedListener: ((FlexAdapterSelectableItem<*>) -> Unit)? = null
}
