package com.github.ajalt.flexadapter

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.ViewGroup
import java.util.*

class FlexAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    interface ItemSwipedListener {
        fun onItemSwiped(item: FlexAdapterItem<*>)
    }

    interface ItemDraggedListener {
        fun onItemDragged(item: FlexAdapterItem<*>, from: Int, to: Int)
    }

    /**
     * Set or clear a listener that will be notified when an item is dismissed with a swipe.
     *
     * The listener is only applicable if you set up your [RecyclerView] to use the [.getItemTouchHelper]
     */
    var itemSwipedListener: ((item: FlexAdapterItem<*>) -> Unit)? = null

    /**
     * A version of [setItemSwipedListener] that takes an interface that's easier to call from Java.
     */
    fun setItemSwipedListener(listener: ItemSwipedListener) {
        itemSwipedListener = { listener.onItemSwiped(it) }
    }

    /**
     * Set or clear a listener that will be notified when an item dragged to a new position.
     *
     * The listener is only applicable if you set up your [RecyclerView] to use the [.getItemTouchHelper]
     */
    var itemDraggedListener: ((item: FlexAdapterItem<*>, from: Int, to: Int) -> Unit)? = null

    /**
     * A version of [setItemDraggedListener] that takes an interface that's easier to call from Java.
     */
    fun setItemDraggedListener(listener: ItemDraggedListener) {
        itemDraggedListener = { item, from, to -> listener.onItemDragged(item, from, to) }
    }

    private var items: MutableList<FlexAdapterItem<out RecyclerView.ViewHolder>> = mutableListOf()
    private val viewHolderFactoriesByItemType = HashMap<Int, (ViewGroup) -> RecyclerView.ViewHolder>()

    /** Remove all items from the adapter */
    fun clear() {
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
        viewHolderFactoriesByItemType.clear()
    }

    /** Remove all existing items and add the given items */
    fun resetItems(items: List<FlexAdapterItem<out RecyclerView.ViewHolder>>) {
        val oldSize = this.items.size
        this.items = items.toMutableList()
        viewHolderFactoriesByItemType.clear()

        if (items.isEmpty()) {
            notifyItemRangeRemoved(0, oldSize)
            return
        }

        for (item in items) recordItemType(item)
        notifyDataSetChanged()
    }

    /** Add a new item to the adapter at the end of the list of current items. */
    fun addItem(item: FlexAdapterItem<out RecyclerView.ViewHolder>) {
        recordItemType(item)
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    /** Add new items to the adapter at the end of the list of current items. */
    fun addItems(items: Collection<FlexAdapterItem<out RecyclerView.ViewHolder>>) {
        for (item in items) {
            recordItemType(item)
        }
        val start = this.items.size
        this.items.addAll(items)
        notifyItemRangeInserted(start, items.size)
    }

    /** Insert a new item into the adapter. */
    fun insertItem(position: Int, item: FlexAdapterItem<out RecyclerView.ViewHolder>) {
        recordItemType(item)
        items.add(position, item)
        notifyItemInserted(position)
    }

    /** Remove an item from the adapter. */
    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    /** Remove an item from the adapter. */
    fun removeItem(item: FlexAdapterItem<out RecyclerView.ViewHolder>) {
        val i = items.indexOf(item)
        if (i >= 0) {
            items.removeAt(i)
            notifyItemRemoved(i)
        }
    }

    /**
     * Move an item from within the adapter.
     *
     * Both arguments must be valid indexes into the list of items.
     */
    fun moveItem(from: Int, to: Int) {
        if (from == to) return

        if (from < to) {
            Collections.rotate(items.subList(from, to + 1), -1)
        } else {
            Collections.rotate(items.subList(to, from + 1), 1)
        }
        notifyItemMoved(from, to)
    }

    /**
     * @param item the item to look for
     *
     * @return the position of the item, or -1 if not found
     */
    fun indexOf(item: FlexAdapterItem<*>): Int = items.indexOf(item)

    /** Return the index of the firs item that matches the predicate, or -1 */
    fun indexOfFirst(predicate: (FlexAdapterItem<*>) -> Boolean): Int = items.indexOfFirst(predicate)

    /**
     * A SpanSizeLookup for grid layouts.
     *
     * If this adapter is attached to a RecyclerView with a grid layout, and any items in the
     * adapter might have custom span sizes, the return value of this method should be passed to
     * [GridLayoutManager.setSpanSizeLookup]
     */
    val spanSizeLookup: GridLayoutManager.SpanSizeLookup
        get() = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int = items[position].span()
        }

    /**
     * An ItemTouchHelper for RecyclerViews.
     *
     * If any of the items in your RecyclerView might support drag or swipe, you should pass your
     * RecyclerView to the [ItemTouchHelper.attachToRecyclerView] method of the
     * returned helper.
     */
    // If an item can't be dragged, don't let it be reordered.
    val itemTouchHelper: ItemTouchHelper
        get() = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                val i = viewHolder.adapterPosition
                if (i < 0 || i >= items.size) {
                    return 0
                }

                val item = items[i]
                return makeMovementFlags(item.dragDirs(), item.swipeDirs())
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                if (from < 0 || from >= items.size ||
                        to < 0 || to >= items.size ||
                        items[to].dragDirs() == 0) {
                    return false
                }

                moveItem(from, to)
                itemDraggedListener?.invoke(items[to], from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val i = viewHolder.adapterPosition
                if (i < 0 || i >= items.size) {
                    return
                }

                itemSwipedListener?.invoke(items[i])
                removeItem(i)
            }
        })

    private fun recordItemType(item: FlexAdapterItem<out RecyclerView.ViewHolder>) {
        val type = item.itemType()
        if (!viewHolderFactoriesByItemType.containsKey(type)) {
            viewHolderFactoriesByItemType.put(type, item.createViewHolder())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return viewHolderFactoriesByItemType[viewType]!!.invoke(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        items[position].bindErasedViewHolder(holder, position)
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = items[position].itemType()
}
