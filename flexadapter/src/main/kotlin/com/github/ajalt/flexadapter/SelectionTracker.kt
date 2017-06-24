package com.github.ajalt.flexadapter

import java.util.*

/**
 * A class that tracks the selection state of items in an adapter.
 *
 * This is useful if you have some items in you adapter that have a toggleable selection state, but that don't
 * keep track of that state themselves.
 *
 * When the selection state of an item is changed, the adapter is notified and the item's view will be
 * rebound. You can check [isSelected] to see what state to display.
 *
 * All items must implement a valid [hashCode] for this class to work correctly.
 */
class SelectionTracker<T : Any>(private val adapter: FlexAdapter<T>) : ItemRemovedListener<T> {
    private var selectedItems: MutableSet<T> = HashSet()

    init {
        adapter.itemRemovedListener = this
    }

    /** Return a set containing any items that are currently selected. */
    fun selectedItems(): Set<T> = selectedItems.toSet()

    /** The number of items currently selected. */
    val selectedItemCount: Int get() = selectedItems.size

    /** Return true if the given item is marked as selected */
    fun isSelected(item: T) = selectedItems.contains(item)

    /**
     * Mark an item as selected.
     *
     * This will cause the view to update.
     *
     * @param item The item to mark as selected.
     * @throws IllegalArgumentException If the [item] is not already in the adapter.
     */
    fun selectItem(item: T) {
        val i = adapter.items.indexOf(item)
        require(i >= 0) { "Cannot select item that is not in adapter." }
        selectedItems.add(item)
        adapter.notifyItemChanged(i)
    }

    /**
     * Mark an item as not selected.
     *
     * This will cause the view to update.
     *
     * If the item is not already in the adapter, this call has no effect.
     *
     * @param item The item to mark as not selected.
     */
    fun deselectItem(item: T) {
        val i = adapter.items.indexOf(item)
        if (i < 0) return
        selectedItems.remove(item)
        adapter.notifyItemChanged(i)
    }

    /**
     * Set all items in this adapter to be deselected
     *
     * The deselected items will remain in the adapter and are otherwise unchanged.
     *
     * If there are no items in the adapter, this call has no effect.
     */
    fun deselectAllItems() {
        if (selectedItems.isEmpty()) return

        for ((i, item) in adapter.items.withIndex()) {
            if (selectedItems.remove(item)) adapter.notifyItemChanged(i)
            if (selectedItems.isEmpty()) return
        }
    }

    /**
     * Set all items in this adapter to be selected
     *
     * The selected items will remain in the adapter and are otherwise unchanged.
     *
     * If there are no items in the adapter, this call has no effect.
     */
    fun selectAllItems() {
        if (selectedItemCount >= adapter.itemCount) return
        for ((i, item) in adapter.items.withIndex()) {
            if (selectedItems.add(item)) {
                adapter.notifyItemChanged(i)
            }
        }
    }

    override fun itemRemoved(item: T) {
        selectedItems.remove(item)
    }

    override fun allItemChanged() {
        if (selectedItems.isEmpty()) return
        val newSelection = HashSet<T>(Math.min(selectedItems.size, adapter.items.size))
        adapter.items.filterTo(newSelection) { it in selectedItems }
        selectedItems = newSelection
    }
}

/** Create a [SelectionTracker] for this adapter. */
fun <T : Any> FlexAdapter<T>.selectionTracker() = SelectionTracker(this)
