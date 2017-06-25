package com.github.ajalt.flexadapter

import com.github.ajalt.flexadapter.internal.ObservableList
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
class SelectionTracker<T : Any> private constructor(private val adapter: FlexAdapter<T>) {
    companion object {
        fun <T : Any> create(adapter: FlexAdapter<T>) = SelectionTracker(adapter).apply {
            adapter.registerListener(listener)
        }
    }

    private var allSelected = false
    /**
     * If [allSelected] == `false`, this is the set of selected items. If [allSelected] == false, this is the
     * set of items that are _not_ selected.
     *
     * This is an optimization of the common case of calling [selectAllItems] to allow it to be done in
     * constant time instead of needing to add the entire list of items to the selected set.
     */
    private var disjointItems: MutableSet<T> = HashSet()

    private val listener = object : ObservableList.OnListChangedCallback<T> {
        override fun onChanged(sender: ObservableList<T>) {
            // The behavior of this function is to retain the selection state of all items that are still in
            // the adapter, while removing the state of any that have been removed from the adapter. This
            // current algorithm is currently sufficient for correctness, since this function only called when
            // items are removed or their position is changed. If the ObservableList ever calls this as the
            // result of items being added, this will incorrectly mark them as selected if allSelected is
            // true.
            if (sender.isEmpty()) {
                allSelected = false
                disjointItems.clear()
                return
            }
            if (disjointItems.isEmpty()) return
            val newSelection = HashSet<T>(minOf(disjointItems.size, adapter.items.size))
            adapter.items.filterTo(newSelection) { it in disjointItems }
            disjointItems = newSelection
        }

        override fun onItemChanged(sender: ObservableList<T>, index: Int, oldItem: T) {
            trackDeselection(oldItem)
        }

        override fun onItemRangeInserted(sender: ObservableList<T>, start: Int, count: Int) {
            if (allSelected) {
                for (i in start..start + count - 1) {
                    trackDeselection(sender[i])
                }
            }
        }

        override fun onItemRangeRemoved(sender: ObservableList<T>, start: Int, count: Int) {
            onChanged(sender)
        }

        override fun onItemRemoved(sender: ObservableList<T>, index: Int, item: T) {
            disjointItems.remove(item)
        }
    }

    /**
     * Return a collection containing any items that are currently selected.
     *
     * Note that this creates a new collection. If you just want to check an item for membership, [isSelected]
     * is faster.
     */
    fun selectedItems(): Collection<T> =
            if (allSelected) adapter.items.filter { it !in disjointItems }
            else HashSet(disjointItems)

    /** The number of items currently selected. */
    val selectedItemCount: Int
        get() {
            return if (allSelected) adapter.items.size - disjointItems.size
            else disjointItems.size
        }

    /** Return true if the given item is marked as selected */
    fun isSelected(item: T) = allSelected != disjointItems.contains(item)

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
        if (allSelected) disjointItems.remove(item)
        else disjointItems.add(item)
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
        trackDeselection(item)
        adapter.notifyItemChanged(i)
    }

    private fun trackDeselection(item: T) {
        if (allSelected) disjointItems.add(item)
        else disjointItems.remove(item)
    }

    /**
     * Set all items in this adapter to be deselected
     *
     * The deselected items will remain in the adapter and are otherwise unchanged.
     *
     * If there are no items in the adapter, this call has no effect.
     */
    fun deselectAllItems() {
        if (allSelected) removeDisjointItemsWithToggle()
        else removeDisjointItemsWithoutToggle()
    }

    /**
     * Set all items in this adapter to be selected
     *
     * The selected items will remain in the adapter and are otherwise unchanged.
     *
     * If there are no items in the adapter, this call has no effect.
     */
    fun selectAllItems() {
        if (allSelected) removeDisjointItemsWithoutToggle()
        else removeDisjointItemsWithToggle()
    }

    private fun removeDisjointItemsWithoutToggle() {
        if (disjointItems.isEmpty()) return
        for ((i, item) in adapter.items.withIndex()) {
            if (disjointItems.remove(item)) adapter.notifyItemChanged(i)
            if (disjointItems.isEmpty()) return
        }
    }

    private fun removeDisjointItemsWithToggle() {
        allSelected = !allSelected
        if (disjointItems.isEmpty()) {
            adapter.notifyItemRangeChanged(0, adapter.items.size)
            return
        }
        if (disjointItems.size >= adapter.items.size) {
            disjointItems.clear()
            adapter.notifyItemRangeChanged(0, adapter.items.size)
            return
        }
        for ((i, item) in adapter.items.withIndex()) {
            if (!disjointItems.remove(item)) adapter.notifyItemChanged(i)
            if (disjointItems.isEmpty()) {
                adapter.notifyItemRangeChanged(i, adapter.items.size - i)
                return
            }
        }
    }
}

/** Create a [SelectionTracker] for this adapter. */
fun <T : Any> FlexAdapter<T>.selectionTracker() = SelectionTracker.create(this)
