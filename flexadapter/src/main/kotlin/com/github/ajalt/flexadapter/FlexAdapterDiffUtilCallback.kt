package com.github.ajalt.flexadapter

import android.support.v7.util.DiffUtil

/**
 * A [DiffUtil.Callback] that operates on two lists of [FlexAdapterItem]s.
 *
 * It is implemented using item type and equality checks, so you will need to implement [equals] on
 * your items for this to function correctly.
 *
 * You could also inherit from this class and override [areContentsTheSame] instead of implementing
 * [equals].
 *
 * Note that using item type as the implementation will cause moves between two of the same item
 * type to be tracked as updates. If you are tracking moves and want different behavior, you'll have
 * to override [areItemsTheSame].
 *
 * @property oldItems The list of items in the adapter before the change. Typically just [FlexAdapter.items].
 * @property newItems The list of items in the adapter after the change.
 */
class FlexAdapterDiffUtilCallback(val oldItems: List<FlexAdapterItem<*>>,
                                  val newItems: List<FlexAdapterItem<*>>) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
            = oldItems[oldItemPosition].itemType == newItems[newItemPosition].itemType

    override fun getOldListSize(): Int = oldItems.size

    override fun getNewListSize(): Int = newItems.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
            = oldItems[oldItemPosition] == newItems[newItemPosition]
}
