package com.github.ajalt.flexadapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

/**
 * An item that holds data for binding in a [FlexAdapter].
 */
abstract class FlexAdapterItem<VH : RecyclerView.ViewHolder> {
    /**
     * Return a function that creates a ViewHolder for items for items whose [.itemType] matches this one.
     *
     * The returned ViewHolder will be bound to different items, so it should not contain
     * item-specific data at creation time. This method should be effectively static.
     *
     * The argument to the returned method is the parent [ViewGroup]
     */
    abstract fun createViewHolder(): (ViewGroup) -> VH

    /**
     * Bind the contents of this item to a view holder.
     *
     * The bound values should be fields on the item instance doing the binding.
     *
     * @see RecyclerView.Adapter.bindViewHolder
     */
    abstract fun bindViewHolder(holder: VH, position: Int)

    /**
     * Return an integer that uniquely identifies items of this class.
     *
     * All items of the same class must return the same value. The default implementation is
     * suitable in most cases.
     * 
     * @see RecyclerView.Adapter.getItemViewType
     */
    open fun itemType(): Int = javaClass.hashCode()

    /**
     * Return the number of columns that this item should span if laid out in a GridManager.
     */
    open fun span(): Int = 1

    /**
     * If this item can be swiped away, it should return the direction flags it supports.
     *
     * A typical return value would be the bitwise OR of [ItemTouchHelper.LEFT]
     * and [ItemTouchHelper.RIGHT]
     *
     * A return value of 0 indicates that this item cannot be swiped.
     */
    open fun swipeDirs(): Int = 0

    /**
     * If this item can be dragged to reorder, it should return the direction flags it
     * supports.
     *
     * A typical return value would be the bitwise OR of [ItemTouchHelper.LEFT]
     * and [ItemTouchHelper.RIGHT]
     */
    open fun dragDirs(): Int = 0

    /** Called by the [FlexAdapter] to work around Java's type erasure */
    @Suppress("UNCHECKED_CAST")
    internal fun bindErasedViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        bindViewHolder(holder as VH, position)
    }
}

