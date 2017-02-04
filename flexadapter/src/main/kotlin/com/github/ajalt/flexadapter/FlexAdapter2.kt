@file:Suppress("unused")

package com.github.ajalt.flexadapter

import android.support.annotation.LayoutRes
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.ajalt.flexadapter.internal.ObservableArrayList
import com.github.ajalt.flexadapter.internal.ObservableList
import java.util.*
import kotlin.reflect.KClass

internal interface FlexAdapterItemAttrs {
    /**
     * Return the number of columns that this item should span if laid out in a GridManager.
     */
    val span: Int
    /**
     * If this item can be swiped away, it should return the direction flags it supports.
     *
     * A typical return value would be the bitwise OR of [ItemTouchHelper.LEFT]
     * and [ItemTouchHelper.RIGHT]
     *
     * A return value of 0 indicates that this item cannot be swiped.
     */
    val swipeDirs: Int
    /**
     * If this item can be dragged to reorder, it should return the direction flags it
     * supports.
     *
     * A typical return value would be the bitwise OR of [ItemTouchHelper.LEFT]
     * and [ItemTouchHelper.RIGHT]
     */
    val dragDirs: Int
}

private data class ItemAttrs(@LayoutRes val layout: Int, override val span: Int,
                             override val swipeDirs: Int, override val dragDirs: Int,
                             val viewBinder: (Any, View, Int) -> Unit) : FlexAdapterItemAttrs

/**
 * A [RecyclerView.Adapter] that handles multiple item layouts with per-item swipe, drag, and span
 * behavior.
 *
 * @param registerAutomatically When true, the adapter will setup the item touch helper whenever it
 *                              is attached to a [RecyclerView]. (default true)
 */
open class FlexAdapter2<T : Any>(private val registerAutomatically: Boolean = true) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    interface ItemSwipedListener<in T> {
        fun onItemSwiped(item: T)
    }

    interface ItemDraggedListener<in T> {
        fun onItemDragged(item: T, from: Int, to: Int)
    }

    private val listListener = object : ObservableList.OnListChangedCallback<ObservableList<T>> {
        var enabled = true
        override fun onChanged(sender: ObservableList<T>) = if (enabled) {
            recordAllItems()
            notifyDataSetChanged()
        } else Unit

        override fun onItemChanged(sender: ObservableList<T>, index: Int, oldItem: Any?) = if (enabled) {
//            selectedItems.remove(oldItem)
            recordItems(index..index)
            notifyItemChanged(index)
        } else Unit

        override fun onItemRangeInserted(sender: ObservableList<T>, start: Int, count: Int) = if (enabled) {
            recordItems(start until start + count)
            notifyItemRangeInserted(start, count)
        } else Unit

        override fun onItemRangeRemoved(sender: ObservableList<T>, start: Int, count: Int) = if (enabled) {
            recordAllItems()
            notifyItemRangeRemoved(start, count)
        } else Unit

        override fun onItemRemoved(sender: ObservableList<T>, index: Int, item: Any?) = if (enabled) {
            markItemRemoved(item)
            notifyItemRemoved(index)
        } else Unit
    }

    private var itemDraggedListener: ((T, Int, Int) -> Unit)? = null
//    private val selectedItems: MutableSet<T> = HashSet()
    private val viewHolderFactoriesByItemType = HashMap<Int, (ViewGroup) -> RecyclerView.ViewHolder>()
    private val itemAttrsByItemType = HashMap<Int, ItemAttrs>()
    private var callDragListenerOnDropOnly: Boolean = true

    /**
     * The items in this adapter.
     *
     * Changes to this list will automatically be reflected in the adapter.
     */
    open val items: MutableList<T> = ObservableArrayList(listListener)

    /** Set or clear a listener that will be notified when an item is dismissed with a swipe. */
    open var itemSwipedListener: ((item: T) -> Unit)? = null

    /** A version of [itemSwipedListener] that takes an interface that's easier to call from Java. */
    open fun setItemSwipedListener(listener: ItemSwipedListener<T>) {
        itemSwipedListener = { listener.onItemSwiped(it) }
    }

    /**
     * Set or clear a listener that will be called when an item in the adapter is dragged.
     *
     * @param listener       The callback that will be called when an item is dragged
     * @param callOnDropOnly If true, the listener will be be called when an item is dropped in new
     *                       location. If false, it will be called while an item is being dragged.
     */
    open fun setItemDraggedListener(callOnDropOnly: Boolean = true, listener: ((item: T, from: Int, to: Int) -> Unit)?) {
        itemDraggedListener = listener
        callDragListenerOnDropOnly = callOnDropOnly
    }

    /**
     * A version of [setItemDraggedListener] that takes an interface that's easier to call from Java.
     */
    @JvmOverloads
    open fun setItemDraggedListener(callOnDropOnly: Boolean = true, listener: ItemDraggedListener<T>) {
        itemDraggedListener = { item, from, to -> listener.onItemDragged(item, from, to) }
        callDragListenerOnDropOnly = callOnDropOnly
    }


//    /** Return a set containing any [FlexAdapterSelectableItem]s that are currently selected. */
//    open fun selectedItems(): Set<T> = selectedItems.toSet()

//    /**
//     * Set all [FlexAdapterSelectableItem]s in this adapter to be deselected
//     *
//     * The deselected items will remain in the adapter and are otherwise unchanged.
//     *
//     * If there are no [FlexAdapterSelectableItem]s in the adapter, this call has no effect.
//     */
//    open fun deselectAllItems() {
//        if (selectedItems.isEmpty()) return
//
//        for ((i, item) in items.withIndex()) {
//            if (selectedItems.remove(item)) {
//                notifyItemChanged(i)
//            }
//            if (selectedItems.isEmpty()) return
//        }
//    }

//    /**
//     * Set all [FlexAdapterSelectableItem]s in this adapter to be selected
//     *
//     * The selected items will remain in the adapter and are otherwise unchanged.
//     *
//     * If there are no [FlexAdapterSelectableItem]s in the adapter, this call has no effect.
//     */
//    open fun selectAllItems() {
//        if (selectedItemCount >= itemCount) return
//        for ((i, item) in items.withIndex()) {
//            if (selectedItems.add(item)) {
//                notifyItemChanged(i)
//            }
//        }
//    }

//    /**
//     * Remove all existing items and add the given items, using [DiffUtil] for update notifications.
//     *
//     * This is an optional version of [resetItems] that can give better update animations when you
//     * have a list of items is slightly changed from the current list of items. It's useful if you
//     * want to take the `items` list, modify it, and update the adapter with the changes.
//     *
//     * Note that this calls [DiffUtil.calculateDiff] and blocks until that function is complete, so
//     * if you have more than around 1000 items in the adapter, you should consider calculating the
//     * diff on a background thread. In that case, call the overload that takes a
//     * [DiffUtil.DiffResult].
//     *
//     * @param items The new list of items that will be present in the adapter.
//     * @param detectMoves If true, perform extra work to detect items that have moved in the list.
//     * @param callback The callback to use. An instance of [FlexAdapterDiffUtilCallback] by default.
//     */
//    open fun resetItemsWithDiff(items: List<FlexAdapterItem<out RecyclerView.ViewHolder>>,
//                                detectMoves: Boolean = false,
//                                callback: DiffUtil.Callback = FlexAdapterDiffUtilCallback(this.items, items)) {
//        if (this.items.isEmpty() || items.isEmpty()) return resetItems(items)
//        resetItemsWithDiff(items, DiffUtil.calculateDiff(callback, detectMoves))
//
//    }
//
//    /**
//     * Remove all existing items and add the given items, using [DiffUtil] for update notifications.
//     *
//     * Use this overload if you want to perform the calculations off of the main thread.
//     *
//     * Note that you must call this instead of [DiffUtil.DiffResult.dispatchUpdatesTo] directly, or
//     * the internal state of the adapter will become inconsistent.
//     *
//     * Example usage with Kotlin, RxJava, and RxAndroid:
//     * ```
//     * Observable.fromCallable {
//     *     DiffUtil.calculateDiff(FlexAdapterDiffUtilCallback(adapter.items(), newIems), false)
//     * }.observeOn(Schedulers.computation())
//     *         .subscribeOn(AndroidSchedulers.mainThread())
//     *         .subscribe { adapter.resetItemsWithDiff(newItems, it) }
//     * ```
//     *
//     * @param items The new list of items that will be present in the adapter.
//     * @param diffResult The result that will dispatch updates to this adapter.
//     */
//    open fun resetItemsWithDiff(items: List<FlexAdapterItem<out RecyclerView.ViewHolder>>,
//                                diffResult: DiffUtil.DiffResult) {
//        resetWithoutNotification(items)
//        diffResult.dispatchUpdatesTo(this)
//    }

    /**
     * Move an item from within the adapter.
     *
     * Both arguments must be valid indexes into the list of items.
     */
    open fun moveItem(from: Int, to: Int) {
        require(from >= 0 && from < items.size) { "Invalid index $from for list of size ${items.size}" }
        require(to >= 0 && to < items.size) { "Invalid index $to for list of size ${items.size}" }

        if (from == to) return

        listListener.enabled = false
        if (from < to) {
            Collections.rotate(items.subList(from, to + 1), -1)
        } else {
            Collections.rotate(items.subList(to, from + 1), 1)
        }
        listListener.enabled = true
        notifyItemMoved(from, to)
    }

    /**
     * A SpanSizeLookup for grid layouts.
     *
     * If this adapter is attached to a RecyclerView with a grid layout, and any items in the
     * adapter might have custom span sizes, the return value of this method should be passed to
     * [GridLayoutManager.setSpanSizeLookup]
     */
    open val spanSizeLookup: GridLayoutManager.SpanSizeLookup
        get() = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int = attrsAt(position).span
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

                val item = attrsAt(i)
                return makeMovementFlags(item.dragDirs, item.swipeDirs)
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                if (from < 0 || from >= items.size ||
                        to < 0 || to >= items.size ||
                        attrsAt(to).dragDirs == 0) {
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
                if (i < 0 || i >= items.size) return

                itemSwipedListener?.invoke(items[i])
                items.removeAt(i)
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


//    /** Return the number of selected items currently in the adapter. */
//    open val selectedItemCount: Int get() = selectedItems.size

    /** Return the number of items currently in the adapter. */
    override fun getItemCount(): Int = items.size

    /**
     * Notify that an item has changed.
     *
     * @see RecyclerView.Adapter.notifyItemChanged
     */
    open fun notifyItemObjectChanged(item: T) = notifyItemChanged(items.indexOf(item))

    private fun recordItems(range: IntRange) {
        for (i in range) {
            (items[i] as? FlexAdapterItem<*>)?.let { recordItemType(it) }
        }
    }

    private fun recordItemType(item: FlexAdapterItem<out RecyclerView.ViewHolder>) {
        val type = item.itemType
        if (!viewHolderFactoriesByItemType.containsKey(type)) {
            viewHolderFactoriesByItemType.put(type, item.viewHolderFactory())
        }
    }

    private fun markItemRemoved(item: Any?) {
        if (item == null) return
//        if (item is FlexAdapterSelectableItem<*>) {
//            @Suppress("UNNECESSARY_NOT_NULL_ASSERTION") selectedItems.remove(item!!)
//        }
    }

    private fun recordAllItems() {
        viewHolderFactoriesByItemType.clear()
//        selectedItems.clear()
        recordItems(0 until items.size)
    }

    /** @suppress */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return viewHolderFactoriesByItemType[viewType]?.invoke(parent) ?: createViewHolderFromAttr(parent, viewType)
    }

    private fun createViewHolderFromAttr(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return FlexAdapterExtensionItem.ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        itemAttrsByItemType[viewType]!!.layout, parent, false))
    }

    /** @suppress */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        items[position].let {
            if (it is FlexAdapterItem<*>) it.bindErasedViewHolder(holder, position)
            else itemAttrsByItemType[it.javaClass.hashCode()]!!.viewBinder(it, holder.itemView, position)
        }
    }

    /** @suppress */
    override fun getItemViewType(position: Int): Int = items[position].let {
        if (it is FlexAdapterItem<*>) it.itemType
        else it.javaClass.hashCode()
    }

    /** @suppress */
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        if (registerAutomatically) {
            itemTouchHelper.attachToRecyclerView(recyclerView)
        }
    }

    /** @suppress */
    open fun registerType(cls: KClass<*>, @LayoutRes layout: Int, span: Int, swipeDirs: Int,
                          dragDirs: Int, viewBinder: (Any, View, Int) -> Unit) {
        itemAttrsByItemType.put(cls.java.hashCode(), ItemAttrs(layout, span, swipeDirs, dragDirs, viewBinder))
    }

    private fun attrsAt(index: Int): FlexAdapterItemAttrs = items[index].let {
        Log.d("FlexAdapter", "attrsAt=$index, id=${it.javaClass.hashCode()}, it=$it, itemAttrsByItemType=$itemAttrsByItemType")
        if (it is FlexAdapterItemAttrs) it
        else itemAttrsByItemType[it.javaClass.hashCode()]!!
    }
}

inline fun <reified T> FlexAdapter2<*>.register(@LayoutRes layout: Int, span: Int = 1, swipeDirs: Int = 0,
                                                dragDirs: Int = 0, crossinline viewBinder: (T, View, Int) -> Unit) {
    registerType(T::class, layout, span, swipeDirs, dragDirs) { any, view, i ->
        viewBinder(any as T, view, i)
    }
}

