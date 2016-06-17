package com.github.ajalt.flexadapter

import android.support.v4.view.PagerAdapter
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import java.util.*


open class FlexPagerAdapter : PagerAdapter() {
    private var items: MutableList<Pair<FlexAdapterItem<out RecyclerView.ViewHolder>, RecyclerView.ViewHolder?>> = mutableListOf()
    private val viewHolderFactoriesByItemType = HashMap<Int, (ViewGroup) -> RecyclerView.ViewHolder>()

    /** Remove all items from the adapter */
    open fun clear() {
        items.clear()
        viewHolderFactoriesByItemType.clear()
        notifyDataSetChanged()
    }

    /** Remove all existing items and add the given items */
    open fun resetItems(items: Collection<FlexAdapterItem<out RecyclerView.ViewHolder>>) {
        viewHolderFactoriesByItemType.clear()
        for (item in items) recordItemType(item)

        this.items = items.mapTo(ArrayList(items.size)) { it to null }

        notifyDataSetChanged()
    }

    /** Add a new item to the adapter at the end of the list of current items. */
    open fun addItem(item: FlexAdapterItem<out RecyclerView.ViewHolder>) {
        recordItemType(item)
        items.add(item to null)
        notifyDataSetChanged()
    }

    /** Add new items to the adapter at the end of the list of current items. */
    open fun addItems(vararg items: FlexAdapterItem<out RecyclerView.ViewHolder>) {
        for (item in items) {
            recordItemType(item)
        }
        val start = this.items.size
        this.items.addAll(items.map { it to null })
        notifyDataSetChanged()
    }

    fun itemAt(position: Int) = items[position]

    fun notifyItemChanged(position: Int) {
        val vh = items[position].second ?: return
        items[position].first.bindErasedViewHolder(vh, position)
    }

    fun notifyItemRangeChanged(positionStart: Int, itemCount: Int) {
        for (i in positionStart..positionStart + itemCount - 1) {
            notifyItemChanged(i)
        }
    }

    /** Return the number of items currently in the adapter. */
    override fun getCount(): Int = items.size

    /** @suppress */
    override fun instantiateItem(container: ViewGroup, position: Int): Any? {
        val vh = items[position].second ?:
                viewHolderFactoriesByItemType[items[position].first.itemType()]!!.invoke(container)
        items[position].first.bindErasedViewHolder(vh, position)
        container.addView(vh.itemView)
        return vh.itemView
    }

    /** @suppress */
    override fun isViewFromObject(view: View?, o: Any?): Boolean = view == o

    /** @suppress */
    override fun destroyItem(container: ViewGroup, position: Int, o: Any?) = container.removeView(o as View)

    private fun recordItemType(item: FlexAdapterItem<out RecyclerView.ViewHolder>) {
        val type = item.itemType()
        if (!viewHolderFactoriesByItemType.containsKey(type)) {
            viewHolderFactoriesByItemType.put(type, item.createViewHolder())
        }
    }
}
