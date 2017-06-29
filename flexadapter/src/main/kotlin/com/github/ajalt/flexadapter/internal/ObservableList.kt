package com.github.ajalt.flexadapter.internal

import android.os.Build
import android.support.annotation.RequiresApi
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.UnaryOperator

/** A list interface that notifies a listener when it's contents change. */
internal interface ObservableList<T> : MutableList<T> {
    interface OnListChangedCallback<T> {
        fun onChanged(sender: ObservableList<T>)
        fun onItemChanged(sender: ObservableList<T>, index: Int, oldItem: T)
        fun onItemRangeInserted(sender: ObservableList<T>, start: Int, count: Int)
        fun onItemRangeRemoved(sender: ObservableList<T>, start: Int, count: Int)
        fun onItemRemoved(sender: ObservableList<T>, index: Int, item: T)
    }

    fun registerListener(listener: ObservableList.OnListChangedCallback<T>)
    fun unregisterListener(listener: ObservableList.OnListChangedCallback<T>)
    fun unregisterAllListeners()
}

internal class ObservableArrayList<T> : ArrayList<T>(), ObservableList<T> {
    private val listeners = WeakHashMap<ObservableList.OnListChangedCallback<T>, Void>()

    override fun registerListener(listener: ObservableList.OnListChangedCallback<T>) {
        listeners.putIfAbsent(listener, null)
    }

    override fun unregisterListener(listener: ObservableList.OnListChangedCallback<T>) {
        listeners.remove(listener)
    }

    override fun unregisterAllListeners() = listeners.clear()

    override fun add(element: T): Boolean = super.add(element).apply { notifyAdd(size - 1, 1) }
    override fun add(index: Int, element: T) = super.add(index, element).apply { notifyAdd(index, 1) }

    override fun addAll(elements: Collection<T>): Boolean {
        val oldSize = size
        val added = super.addAll(elements)
        if (added) {
            notifyAdd(oldSize, size - oldSize)
        }
        return added
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean
            = super.addAll(index, elements).apply { if (this) notifyAdd(index, elements.size) }

    override fun clear() {
        val oldSize = size
        super.clear()
        if (oldSize != 0) {
            notifyRangeRemove(0, oldSize)
        }
    }

    override fun set(index: Int, element: T): T = super.set(index, element).apply { notifyChange(index, this) }

    override fun removeAt(index: Int): T = super.removeAt(index).apply { notifyRemove(index, this) }

    override fun remove(element: T): Boolean {
        val index = indexOf(element)
        if (index >= 0) {
            removeAt(index)
            return true
        }
        return false
    }

    override fun removeAll(elements: Collection<T>): Boolean
            = super.removeAll(elements).apply { notifyAllChange() }

    override fun retainAll(elements: Collection<T>): Boolean
            = super.retainAll(elements).apply { notifyAllChange() }

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        if (toIndex == fromIndex) return
        // In the case of single element removal, we can notify with the removed item
        if (toIndex == fromIndex + 1) {
            removeAt(fromIndex)
        } else {
            super.removeRange(fromIndex, toIndex)
            notifyRangeRemove(fromIndex, toIndex - fromIndex)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun forEach(action: Consumer<in T>?) = super<ArrayList>.forEach(action)

    @RequiresApi(Build.VERSION_CODES.N)
    override fun spliterator(): Spliterator<T> {
        return super<ArrayList>.spliterator()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun replaceAll(operator: UnaryOperator<T>)
            = super<ArrayList>.replaceAll(operator).apply { notifyAllChange() }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun sort(c: Comparator<in T>?)
            = super<ArrayList>.sort(c).apply { notifyAllChange() }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun removeIf(filter: Predicate<in T>): Boolean
            = super<ArrayList>.removeIf(filter).apply { notifyAllChange() }

    private fun notifyAdd(start: Int, count: Int) = listeners.forEach { it.key.onItemRangeInserted(this, start, count) }
    private fun notifyRangeRemove(start: Int, count: Int) = listeners.forEach { it.key.onItemRangeRemoved(this, start, count) }
    private fun notifyRemove(index: Int, item: T) = listeners.forEach { it.key.onItemRemoved(this, index, item) }
    private fun notifyChange(index: Int, oldItem: T) = listeners.forEach { it.key.onItemChanged(this, index, oldItem) }
    private fun notifyAllChange() = listeners.forEach { it.key.onChanged(this) }
}

