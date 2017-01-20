@file:Suppress("unused")

package com.github.ajalt.flexadapter

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.ViewGroup
import java.util.*

/**
 * A [RecyclerView.Adapter] that handles multiple item layouts with per-item swipe, drag, and span
 * behavior.
 *
 * @param registerAutomatically When true, the adapter will setup the item touch helper whenever it
 *                              is attached to a [RecyclerView]. (default true)
 */
open class FlexAdapter(private val registerAutomatically: Boolean = true) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface ItemSwipedListener {
        fun onItemSwiped(item: FlexAdapterItem<*>)
    }

    interface ItemDraggedListener {
        fun onItemDragged(item: FlexAdapterItem<*>, from: Int, to: Int)
    }

    private var itemDraggedListener: ((FlexAdapterItem<*>, Int, Int) -> Unit)? = null
    private var items: MutableList<FlexAdapterItem<out RecyclerView.ViewHolder>> = mutableListOf()
    private val selectedItems: MutableSet<FlexAdapterSelectableItem<out RecyclerView.ViewHolder>> = HashSet()
    private val viewHolderFactoriesByItemType = HashMap<Int, (ViewGroup) -> RecyclerView.ViewHolder>()
    private var callDragListenerOnDropOnly: Boolean = true

    /** Set or clear a listener that will be notified when an item is dismissed with a swipe. */
    open var itemSwipedListener: ((item: FlexAdapterItem<*>) -> Unit)? = null

    /** A version of [itemSwipedListener] that takes an interface that's easier to call from Java. */
    open fun setItemSwipedListener(listener: ItemSwipedListener) {
        itemSwipedListener = { listener.onItemSwiped(it) }
    }

    /**
     * Set or clear a listener that will be called when an item in the adapter is dragged.
     *
     * @param listener       The callback that will be called when an item is dragged
     * @param callOnDropOnly If true, the listener will be be called when an item is dropped in new
     *                       location. If false, it will be called while an item is being dragged.
     */
    open fun setItemDraggedListener(callOnDropOnly: Boolean = true, listener: ((item: FlexAdapterItem<*>, from: Int, to: Int) -> Unit)?) {
        itemDraggedListener = listener
        callDragListenerOnDropOnly = callOnDropOnly
    }

    /**
     * A version of [setItemDraggedListener] that takes an interface that's easier to call from Java.
     */
    @JvmOverloads
    open fun setItemDraggedListener(callOnDropOnly: Boolean = true, listener: ItemDraggedListener) {
        itemDraggedListener = { item, from, to -> listener.onItemDragged(item, from, to) }
        callDragListenerOnDropOnly = callOnDropOnly
    }

    /** Remove all items from the adapter */
    open fun clear() {
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
        viewHolderFactoriesByItemType.clear()
    }

    /**
     * Return a copy of the list of items in this adapter.
     *
     * You can modify the returned list, but changes won't be reflected in the adapter unless you
     * pass the list to [resetItems].
     */
    open fun items(): MutableList<FlexAdapterItem<out RecyclerView.ViewHolder>> = items.toMutableList()

    /** Return a set containing any [FlexAdapterSelectableItem]s that are currently selected. */
    open fun selectedItems(): Set<FlexAdapterItem<out RecyclerView.ViewHolder>> = selectedItems.toMutableSet()

    /**
     * Set all [FlexAdapterSelectableItem]s in this adapter to be deselected
     *
     * The deselected items will remain in the adapter and are otherwise unchanged.
     *
     * If there are no [FlexAdapterSelectableItem]s in the adapter, this call has no effect.
     */
    open fun deselectAllItems() {
        selectedItems.clear()
        for ((i, item) in items.withIndex()) {
            if (item is FlexAdapterSelectableItem && item.selected) {
                item.selected = false
                notifyItemChanged(i)
            }
        }
    }

    /**
     * Set all [FlexAdapterSelectableItem]s in this adapter to be selected
     *
     * The selected items will remain in the adapter and are otherwise unchanged.
     *
     * If there are no [FlexAdapterSelectableItem]s in the adapter, this call has no effect.
     */
    open fun selectAllItems() {
        if (selectedItemCount == itemCount) return
        for ((i, item) in items.withIndex()) {
            if (item is FlexAdapterSelectableItem && !item.selected) {
                item.selected = true
                notifyItemChanged(i)
            }
        }
    }

    /** Remove all existing items and add the given items */
    open fun resetItems(items: Collection<FlexAdapterItem<out RecyclerView.ViewHolder>>) {
        val oldSize = this.items.size
        this.items = items.toMutableList()
        selectedItems.clear()
        viewHolderFactoriesByItemType.clear()

        if (items.isEmpty()) {
            notifyItemRangeRemoved(0, oldSize)
            return
        }

        for (item in items) recordItemType(item)
        notifyDataSetChanged()
    }

    /** Add a new item to the adapter at the end of the list of current items. */
    open fun addItem(item: FlexAdapterItem<out RecyclerView.ViewHolder>) {
        recordItemType(item)
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    /** Add new items to the adapter at the end of the list of current items. */
    open fun addItems(vararg items: FlexAdapterItem<out RecyclerView.ViewHolder>) {
        if (items.isEmpty()) return

        for (item in items) {
            recordItemType(item)
        }
        val start = this.items.size
        this.items.addAll(items)
        notifyItemRangeInserted(start, items.size)
    }

    /** Insert a new item into the adapter. */
    open fun insertItem(position: Int, item: FlexAdapterItem<out RecyclerView.ViewHolder>) {
        recordItemType(item)
        items.add(position, item)
        notifyItemInserted(position)
    }

    /** Return an existing item at the given position. */
    open fun getItem(position: Int): FlexAdapterItem<out RecyclerView.ViewHolder> = items[position]

    /** Remove an item from the adapter. */
    open fun removeItem(position: Int) {
        val item = items.removeAt(position)
        selectedItems.remove(item)
        notifyItemRemoved(position)
    }

    /**
     * Remove an item from the adapter.
     *
     * If the item is not contained in the adapter, no action is taken.
     */
    open fun removeItem(item: FlexAdapterItem<out RecyclerView.ViewHolder>) {
        val i = items.indexOf(item)
        if (i >= 0) {
            val removedItem = items.removeAt(i)
            selectedItems.remove(removedItem)
            notifyItemRemoved(i)
        }
    }

    /**
     * Move an item from within the adapter.
     *
     * Both arguments must be valid indexes into the list of items.
     */
    open fun moveItem(from: Int, to: Int) {
        require(from >= 0 && from < items.size) { "Invalid index $from for list of size ${items.size}" }
        require(to >= 0 && to < items.size) { "Invalid index $to for list of size ${items.size}" }

        if (from == to) return

        if (from < to) {
            Collections.rotate(items.subList(from, to + 1), -1)
        } else {
            Collections.rotate(items.subList(to, from + 1), 1)
        }
        notifyItemMoved(from, to)
    }

    /** Replace the item at [index] with [newItem] */
    open fun swapItem(index: Int, newItem: FlexAdapterItem<out RecyclerView.ViewHolder>) {
        recordItemType(newItem)
        selectedItems.remove(items[index])
        items[index] = newItem
        notifyItemChanged(index)
    }

    /** Returns first index of [item], or -1 if the adapter does not contain item. */
    open fun indexOf(item: FlexAdapterItem<out RecyclerView.ViewHolder>): Int = items.indexOf(item)

    /** Returns index of the first element matching the given [predicate], or -1 if the list does not contain such element. */
    open fun indexOfFirst(predicate: (FlexAdapterItem<out RecyclerView.ViewHolder>) -> Boolean): Int = items.indexOfFirst(predicate)

    /** Returns the first element matching the given [predicate], or `null` if no such element was found. */
    open fun find(predicate: (FlexAdapterItem<out RecyclerView.ViewHolder>) -> Boolean):
            FlexAdapterItem<out RecyclerView.ViewHolder>? = items.find(predicate)

    /** Notify the adapter that [item] has changed nad need to be redrawn. */
    open fun notifyItemChanged(item: FlexAdapterItem<out RecyclerView.ViewHolder>) =
            notifyItemChanged(indexOf(item))

    /**
     * A SpanSizeLookup for grid layouts.
     *
     * If this adapter is attached to a RecyclerView with a grid layout, and any items in the
     * adapter might have custom span sizes, the return value of this method should be passed to
     * [GridLayoutManager.setSpanSizeLookup]
     */
    open val spanSizeLookup: GridLayoutManager.SpanSizeLookup
        get() = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int = items[position].span()
        }

    /**
     * An ItemTouchHelper for RecyclerViews.
     *
     * If any of the items in your RecyclerView might support drag or swipe, you should pass your
     * RecyclerView to the [ItemTouchHelper.attachToRecyclerView] method of the returned helper.
     */
    open val itemTouchHelper: ItemTouchHelper
        get() = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            private var dragFrom: Int = -1
            private var dragTo: Int = -1

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
                    dragFrom = -1
                    dragTo = -1
                    return false
                }

                dragTo = to
                if (dragFrom < 0) dragFrom = from

                moveItem(from, to)

                if (!callDragListenerOnDropOnly) {
                    itemDraggedListener?.invoke(items[to], from, to)
                }
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

            override fun clearView(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?) {
                super.clearView(recyclerView, viewHolder)

                if (callDragListenerOnDropOnly && dragFrom >= 0 && dragTo >= 0 && dragFrom != dragTo) {
                    itemDraggedListener?.invoke(items[dragTo], dragFrom, dragTo)
                }

                dragFrom = -1
                dragTo = -1
            }
        })

    private fun recordItemType(item: FlexAdapterItem<out RecyclerView.ViewHolder>) {
        val type = item.itemType()
        if (!viewHolderFactoriesByItemType.containsKey(type)) {
            viewHolderFactoriesByItemType.put(type, item.viewHolderFactory())
        }
        if (item is FlexAdapterSelectableItem) {
            if (item.selected) selectedItems.add(item)
            item.selectionChangedListener = itemSelectionChanged
        }
    }

    /** Return the number of selected items currently in the adapter. */
    open val selectedItemCount: Int get() = selectedItems.size

    /** Return the number of items currently in the adapter. */
    override fun getItemCount(): Int = items.size

    /** @suppress */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return viewHolderFactoriesByItemType[viewType]!!.invoke(parent)
    }

    /** @suppress */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        items[position].bindErasedViewHolder(holder, position)
    }

    /** @suppress */
    override fun getItemViewType(position: Int): Int = items[position].itemType()

    /** @suppress */
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        if (registerAutomatically) {
            itemTouchHelper.attachToRecyclerView(recyclerView)
        }
    }

    private val itemSelectionChanged: (FlexAdapterSelectableItem<*>) -> Unit = {
        if (it.selected) {
            selectedItems.add(it)
        } else {
            selectedItems.remove(it)
        }
    }
}
